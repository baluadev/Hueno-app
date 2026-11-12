package com.hueno.camera

import android.content.Context
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.ColorSpaceTransform
import android.hardware.camera2.params.RggbChannelVector
import android.os.SystemClock
import android.util.Log
import android.util.Size
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
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
import com.hueno.core.color.Calibration
import com.hueno.core.color.CalibrationProblem
import com.hueno.core.color.ColorMath
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.ColorReading
import com.hueno.core.color.ReadingProcessor
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Something the camera could not do. The app turns each one into a message. */
enum class CameraIssue {
    START_FAILED,
    LOCK_REJECTED,
    /** The surface in the ring was colored or uneven, so not usable to calibrate on. */
    CALIBRATION_FAILED,
    /** The surface in the ring was so bright that the camera lost its color. */
    CALIBRATION_TOO_BRIGHT,
    /** Too little light on the surface in the ring to calibrate on. */
    CALIBRATION_TOO_DARK,
    /** A saved calibration could not be applied on this camera. */
    PROFILE_FAILED,
    /** The calibration was dropped because the app left the screen and the camera was closed. */
    CALIBRATION_CLEARED_PAUSED,
    /** The calibration was dropped because the torch changed the light. */
    CALIBRATION_CLEARED_TORCH,
}

/** Live state of the color camera. */
data class CameraColorState(
    val reading: ColorReading? = null,
    /** Exposure and white balance are held at fixed values. */
    val locked: Boolean = false,
    /** Milliseconds from [ColorCamera.start] to the first reading; null until one arrives. */
    val firstReadingMs: Long? = null,
    /** Frames analyzed per second, averaged over the last second. */
    val analyzedFps: Double = 0.0,
    val issue: CameraIssue? = null,
    /**
     * This camera can be put back into saved exposure and white-balance settings, which saved
     * calibration profiles need. False on cameras without manual control.
     */
    val supportsProfiles: Boolean = false,
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
    /** Frames left before locking for a recalibration; negative when none is pending. */
    @Volatile private var relockCountdown = -1
    @Volatile private var clearCalibrationRequested = false
    @Volatile private var resetRequested = false
    @Volatile private var calibrationToUse: Calibration? = null

    /** The calibration in effect, mirrored for reads from the UI thread. */
    @Volatile private var activeCalibration: Calibration? = null

    /** The most recent camera settings reported by the sensor, used when saving a profile. */
    @Volatile private var lastResult: TotalCaptureResult? = null

    /** Binds preview and analysis to [owner] and starts producing readings. */
    @OptIn(ExperimentalCamera2Interop::class)
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
                val analysisBuilder = ImageAnalysis.Builder()
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
                // Listens to what the sensor actually used for each frame.
                Camera2Interop.Extender(analysisBuilder).setSessionCaptureCallback(
                    object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult,
                        ) {
                            lastResult = result
                        }
                    },
                )
                val analysis = analysisBuilder.build()
                analysis.setAnalyzer(analysisExecutor, ::analyze)

                cameraProvider.unbindAll()
                resetRequested = true
                val bound = cameraProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                camera = bound
                _state.value = _state.value.copy(issue = null, locked = false, supportsProfiles = hasManualControl(bound))
            } catch (e: Exception) {
                Log.e(TAG, "Could not start the camera", e)
                _state.value = _state.value.copy(issue = CameraIssue.START_FAILED)
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    /** Saved profiles need both manual exposure and manual color correction. */
    @OptIn(ExperimentalCamera2Interop::class)
    private fun hasManualControl(camera: Camera): Boolean = try {
        val capabilities = Camera2CameraInfo.from(camera.cameraInfo)
            .getCameraCharacteristic(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        capabilities != null &&
            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_SENSOR in capabilities &&
            CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_MANUAL_POST_PROCESSING in capabilities
    } catch (e: Exception) {
        false
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
                activeCalibration = null
            }
            calibrationToUse?.let { saved ->
                calibrationToUse = null
                processor.useCalibration(saved)
                activeCalibration = saved
            }

            val reading = processor.process(sample)

            // Recalibrating: automatic exposure and white balance have had time to settle on
            // the new surface, so lock them now and measure once the lock has taken effect.
            val relock = relockCountdown
            if (relock > 0) {
                relockCountdown = relock - 1
            } else if (relock == 0) {
                relockCountdown = -1
                ContextCompat.getMainExecutor(appContext).execute { setLocked(true) }
                calibrateCountdown = SETTLE_FRAMES
            }

            // Calibrate only after the lock has had time to take effect and the smoothing
            // window holds nothing but post-lock frames.
            val countdown = calibrateCountdown
            if (countdown > 0) {
                calibrateCountdown = countdown - 1
            } else if (countdown == 0) {
                calibrateCountdown = -1
                // Raw numbers for tuning the white-reference check on real devices.
                val m = processor.lastMeasured ?: reading.linear
                Log.i(
                    TAG,
                    "calibrate ref r=%.3f g=%.3f b=%.3f ratio=%.3f ringLum=%.3f sceneLum=%.3f spread=%.3f clipped=%.3f".format(
                        java.util.Locale.US, m.r, m.g, m.b,
                        maxOf(m.r, m.g, m.b) / minOf(m.r, m.g, m.b).coerceAtLeast(1e-6),
                        m.luminance, sample.sceneLuminance, sample.areaSpread, sample.clippedFraction,
                    ),
                )
                if (processor.calibrateFromCurrent()) {
                    activeCalibration = processor.calibration
                } else {
                    val issue = when (processor.lastCalibrationProblem) {
                        CalibrationProblem.TOO_BRIGHT -> CameraIssue.CALIBRATION_TOO_BRIGHT
                        CalibrationProblem.TOO_DARK -> CameraIssue.CALIBRATION_TOO_DARK
                        else -> CameraIssue.CALIBRATION_FAILED
                    }
                    // Without a calibration the lock only freezes the camera at settings chosen
                    // for the rejected surface, so give exposure and white balance back.
                    ContextCompat.getMainExecutor(appContext).execute { setLocked(false, issue) }
                }
            }
            val now = SystemClock.elapsedRealtime()

            fpsFrames++
            if (fpsWindowStart == 0L) fpsWindowStart = now
            if (now - fpsWindowStart >= 1000) {
                fps = fpsFrames * 1000.0 / (now - fpsWindowStart)
                fpsFrames = 0
                fpsWindowStart = now
                logSensorState()
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
     * Names a spot in a frozen frame. [argbPixels] are the pixels around the spot, taken from
     * the picture on screen. The current calibration is applied, and the confidence is the one
     * the frame was frozen with, since the light is the same across the frame.
     */
    fun readingFromPixels(argbPixels: IntArray): ColorReading? {
        val base = _state.value.reading ?: return null
        if (argbPixels.isEmpty()) return null
        val calibration = activeCalibration
        val mean = ColorMath.averageLinear(argbPixels)
        val corrected = calibration?.apply(mean) ?: mean
        val rgb = ColorMath.toSrgb(corrected)
        return ColorReading(rgb, ColorNamer.name(rgb), base.confidence, corrected, base.jitter, calibration != null)
    }

    /**
     * Holds or releases auto exposure and auto white balance.
     * Some devices accept the request and still drift; callers should treat the lock as a request
     * and watch the readings.
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun setLocked(locked: Boolean) = setLocked(locked, issueAfter = null)

    /** [issueAfter] is published once the request has gone through, replacing any older issue. */
    @OptIn(ExperimentalCamera2Interop::class)
    private fun setLocked(locked: Boolean, issueAfter: CameraIssue?) {
        val control = camera?.cameraControl ?: return
        val camera2 = Camera2CameraControl.from(control)
        val result = if (locked) {
            camera2.setCaptureRequestOptions(
                CaptureRequestOptions.Builder()
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, true)
                    .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, true)
                    .build(),
            )
        } else {
            // Drops every override, including manual settings from a saved profile.
            camera2.clearCaptureRequestOptions()
        }
        result.addListener({
            try {
                result.get()
                _state.value = _state.value.copy(locked = locked, issue = issueAfter)
            } catch (e: Exception) {
                Log.w(TAG, "Lock request failed", e)
                _state.value = if (!locked && issueAfter != null) {
                    // Unlocking a camera that is closing can be refused; the options are cleared
                    // either way, and the user still needs to hear why the calibration went.
                    _state.value.copy(locked = false, issue = issueAfter)
                } else {
                    _state.value.copy(issue = CameraIssue.LOCK_REJECTED)
                }
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    /**
     * Locks exposure and white balance, then uses whatever is in the ring as the white reference.
     * Point the ring at white paper first.
     */
    fun calibrate() {
        if (_state.value.locked) {
            // Calibrating again. The camera is still held at the old light's settings; measuring
            // under them would judge the new surface with the old white balance (paper read
            // 1.45 instead of 1.13 that way). Release, let the camera adjust, then lock.
            calibrateCountdown = -1
            calibrationToUse = null
            activeCalibration = null
            clearCalibrationRequested = true
            setLocked(false)
            relockCountdown = RELEASE_FRAMES
        } else {
            setLocked(true)
            calibrateCountdown = SETTLE_FRAMES
        }
    }

    /** True while a calibration is in effect or about to be measured. */
    val isCalibrated: Boolean
        get() = activeCalibration != null || calibrateCountdown >= 0 || relockCountdown >= 0 ||
            calibrationToUse != null

    /**
     * Drops the calibration and returns the camera to automatic exposure and white balance.
     * [reason] is shown to the user when the app, not the user, dropped it.
     */
    fun clearCalibration(reason: CameraIssue? = null) {
        calibrateCountdown = -1
        relockCountdown = -1
        calibrationToUse = null
        // Mirrored at once, so a frozen frame read right after this is not corrected any more.
        activeCalibration = null
        clearCalibrationRequested = true
        resetRequested = true
        if (camera == null) {
            _state.value = _state.value.copy(locked = false, issue = reason ?: _state.value.issue)
        } else {
            setLocked(false, reason)
        }
    }

    /**
     * Captures the current calibration together with the camera settings it depends on.
     * Returns null when there is no calibration, the camera cannot restore settings, or the
     * sensor has not reported them.
     */
    fun snapshotProfile(name: String): CalibrationProfile? {
        val calibration = activeCalibration ?: return null
        if (!_state.value.supportsProfiles) return null
        val result = lastResult ?: return null
        val exposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME) ?: return null
        val sensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: return null
        val gains = result.get(CaptureResult.COLOR_CORRECTION_GAINS) ?: return null
        val transform = result.get(CaptureResult.COLOR_CORRECTION_TRANSFORM) ?: return null
        val elements = IntArray(18)
        transform.copyElements(elements, 0)
        return CalibrationProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            calibration = calibration,
            exposureNanos = exposure,
            sensitivity = sensitivity,
            colorGains = listOf(gains.red, gains.greenEven, gains.greenOdd, gains.blue),
            colorTransform = elements.toList(),
        ).takeIf { it.isUsable() }
    }

    /**
     * Puts the camera back into the settings a profile was measured with and uses its
     * calibration. Correct only under the same light the profile was saved in.
     */
    @OptIn(ExperimentalCamera2Interop::class)
    fun applyProfile(profile: CalibrationProfile) {
        val control = camera?.cameraControl
        if (control == null || !_state.value.supportsProfiles || !profile.isUsable()) {
            _state.value = _state.value.copy(issue = CameraIssue.PROFILE_FAILED)
            return
        }
        val g = profile.colorGains
        val options = CaptureRequestOptions.Builder()
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF)
            .setCaptureRequestOption(CaptureRequest.SENSOR_EXPOSURE_TIME, profile.exposureNanos)
            .setCaptureRequestOption(CaptureRequest.SENSOR_SENSITIVITY, profile.sensitivity)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_OFF)
            .setCaptureRequestOption(
                CaptureRequest.COLOR_CORRECTION_MODE,
                CameraMetadata.COLOR_CORRECTION_MODE_TRANSFORM_MATRIX,
            )
            .setCaptureRequestOption(CaptureRequest.COLOR_CORRECTION_GAINS, RggbChannelVector(g[0], g[1], g[2], g[3]))
            .setCaptureRequestOption(
                CaptureRequest.COLOR_CORRECTION_TRANSFORM,
                ColorSpaceTransform(profile.colorTransform.toIntArray()),
            )
            .build()
        calibrateCountdown = -1
        relockCountdown = -1
        val result = Camera2CameraControl.from(control).setCaptureRequestOptions(options)
        result.addListener({
            try {
                result.get()
                calibrationToUse = profile.calibration
                resetRequested = true
                _state.value = _state.value.copy(locked = true, issue = null)
            } catch (e: Exception) {
                Log.w(TAG, "Profile could not be applied", e)
                _state.value = _state.value.copy(issue = CameraIssue.PROFILE_FAILED)
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    /**
     * Once a second while locked: what the sensor reports it is doing, to check on real devices
     * whether the exposure and white-balance lock actually holds.
     */
    private fun logSensorState() {
        if (!_state.value.locked) return
        val result = lastResult ?: return
        val raw = processor.lastMeasured ?: return
        val gains = result.get(CaptureResult.COLOR_CORRECTION_GAINS)
        Log.i(
            TAG,
            "lock raw r=%.3f g=%.3f b=%.3f awbState=%s aeState=%s awbLock=%s aeLock=%s exp=%s iso=%s gains=%s".format(
                java.util.Locale.US, raw.r, raw.g, raw.b,
                result.get(CaptureResult.CONTROL_AWB_STATE), result.get(CaptureResult.CONTROL_AE_STATE),
                result.get(CaptureResult.CONTROL_AWB_LOCK), result.get(CaptureResult.CONTROL_AE_LOCK),
                result.get(CaptureResult.SENSOR_EXPOSURE_TIME), result.get(CaptureResult.SENSOR_SENSITIVITY),
                gains?.let { "%.3f/%.3f/%.3f".format(java.util.Locale.US, it.red, it.greenEven, it.blue) },
            ),
        )
    }

    /** Hides [issue] if it is still the one shown. For notices that only need a moment. */
    fun dismissIssue(issue: CameraIssue) {
        val current = _state.value
        if (current.issue == issue) _state.value = current.copy(issue = null)
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

        /** Frames, about one second, for exposure and white balance to adjust before relocking. */
        const val RELEASE_FRAMES = 30
    }
}
