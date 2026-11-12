package com.hueno.camera

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.os.SystemClock
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.hueno.core.color.ColorReading
import com.hueno.core.color.ReadingProcessor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Live state of the color camera. */
data class CameraColorState(
    val reading: ColorReading? = null,
    /** Exposure and white balance are held at fixed values. */
    val locked: Boolean = false,
    /** Milliseconds from [ColorCamera.start] to the first reading; null until one arrives. */
    val firstReadingMs: Long? = null,
    /** Frames analyzed per second, averaged over the last second. */
    val analyzedFps: Double = 0.0,
    val error: String? = null,
)

/**
 * Runs the camera and turns frames into [ColorReading]s.
 *
 * Frames are analyzed on a single background thread; the UI only ever sees [state].
 * One instance serves one screen: call [start] when the screen is shown and [shutdown] when it
 * goes away for good.
 */
class ColorCamera(private val context: Context) {

    private val appContext = context.applicationContext
    private val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private val processor = ReadingProcessor()

    private val _state = MutableStateFlow(CameraColorState())
    val state: StateFlow<CameraColorState> = _state.asStateFlow()

    @Volatile
    var sampleSize: SampleSize = SampleSize.MEDIUM

    /** While true the published reading is held still, so the user can read or capture it. */
    @Volatile
    var frozen: Boolean = false

    private var camera: Camera? = null
    private var provider: ProcessCameraProvider? = null
    private var startedAt = 0L

    // Touched only on the analysis thread.
    private var lastPublishAt = 0L
    private var fpsWindowStart = 0L
    private var fpsFrames = 0
    private var fps = 0.0

    // Requests from the UI thread, carried out on the analysis thread.
    /** Frames left before calibrating; negative when no calibration is pending. */
    @Volatile private var calibrateCountdown = -1
    @Volatile private var clearCalibrationRequested = false
    @Volatile private var resetRequested = false

    /** Binds preview and analysis to [owner] and starts producing readings. */
    fun start(owner: LifecycleOwner, previewView: PreviewView) {
        startedAt = SystemClock.elapsedRealtime()
        val future = ProcessCameraProvider.getInstance(appContext)
        future.addListener({
            try {
                val cameraProvider = future.get()
                provider = cameraProvider

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                // Color sampling needs few pixels; a small frame keeps analysis fast.
                val analysis = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setResolutionSelector(
                        ResolutionSelector.Builder()
                            .setResolutionStrategy(
                                ResolutionStrategy(
                                    Size(640, 480),
                                    ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER,
                                ),
                            )
                            .build(),
                    )
                    .build()
                analysis.setAnalyzer(analysisExecutor, ::analyze)

                cameraProvider.unbindAll()
                resetRequested = true
                camera = cameraProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                _state.value = _state.value.copy(error = null, locked = false)
            } catch (e: Exception) {
                Log.e(TAG, "Could not start the camera", e)
                _state.value = _state.value.copy(error = e.message ?: "Could not start the camera")
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    private fun analyze(image: ImageProxy) {
        image.use {
            if (resetRequested) {
                resetRequested = false
                processor.reset()
            }
            val plane = it.planes[0]
            val sample = RingSampler.sample(
                buffer = plane.buffer,
                width = it.width,
                height = it.height,
                rowStride = plane.rowStride,
                pixelStride = plane.pixelStride,
                size = sampleSize,
            ) ?: return

            if (clearCalibrationRequested) {
                clearCalibrationRequested = false
                processor.clearCalibration()
            }
            val reading = processor.process(sample)

            // Calibrate only after the lock has had time to take effect and the smoothing
            // window holds nothing but post-lock frames.
            val countdown = calibrateCountdown
            if (countdown > 0) {
                calibrateCountdown = countdown - 1
            } else if (countdown == 0) {
                calibrateCountdown = -1
                if (!processor.calibrateFromCurrent()) {
                    _state.value = _state.value.copy(error = CALIBRATION_FAILED)
                }
            }
            val now = SystemClock.elapsedRealtime()

            fpsFrames++
            if (fpsWindowStart == 0L) fpsWindowStart = now
            if (now - fpsWindowStart >= 1000) {
                fps = fpsFrames * 1000.0 / (now - fpsWindowStart)
                fpsFrames = 0
                fpsWindowStart = now
            }

            // The UI does not need more than about ten updates a second.
            if (!frozen && now - lastPublishAt >= PUBLISH_INTERVAL_MS) {
                lastPublishAt = now
                val current = _state.value
                _state.value = current.copy(
                    reading = reading,
                    firstReadingMs = current.firstReadingMs ?: (now - startedAt),
                    analyzedFps = fps,
                )
            }
        }
    }

    /**
     * Holds or releases auto exposure and auto white balance.
     * Some devices accept the request and still drift; callers should treat the lock as a request
     * and watch the readings.
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun setLocked(locked: Boolean) {
        val control = camera?.cameraControl ?: return
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, locked)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, locked)
            .build()
        val result = Camera2CameraControl.from(control).setCaptureRequestOptions(options)
        result.addListener({
            try {
                result.get()
                _state.value = _state.value.copy(locked = locked, error = null)
            } catch (e: Exception) {
                Log.w(TAG, "Lock request failed", e)
                _state.value = _state.value.copy(error = "This camera did not accept the lock")
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    /**
     * Locks exposure and white balance, then uses whatever is in the ring as the white reference.
     * Point the ring at white paper first.
     */
    fun calibrate() {
        setLocked(true)
        calibrateCountdown = SETTLE_FRAMES
    }

    /** Drops the calibration and returns the camera to automatic exposure and white balance. */
    fun clearCalibration() {
        calibrateCountdown = -1
        clearCalibrationRequested = true
        setLocked(false)
    }

    fun setTorch(on: Boolean) {
        camera?.cameraControl?.enableTorch(on)
    }

    /** Releases the camera and the analysis thread. The instance cannot be used afterwards. */
    fun shutdown() {
        provider?.unbindAll()
        camera = null
        analysisExecutor.shutdown()
    }

    private companion object {
        const val TAG = "ColorCamera"
        const val PUBLISH_INTERVAL_MS = 100L

        /** Frames to wait between asking for the lock and measuring the white reference. */
        const val SETTLE_FRAMES = 20
        const val CALIBRATION_FAILED = "That did not look like white paper. Fill the ring with a white surface and try again."
    }
}
