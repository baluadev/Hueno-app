package com.hueno.core.color

/** What the camera measured inside the sampling ring for one frame. */
data class FrameSample(
    /** Mean color of the ring in linear light, before calibration. */
    val mean: LinearRgb,
    /** Share of sampled pixels with a channel at the top of the range. */
    val clippedFraction: Double,
    /** Standard deviation of luminance across the ring. */
    val areaSpread: Double,
    /** Mean luminance of the whole frame, used to judge whether the scene is too dark. */
    val sceneLuminance: Double,
)

/** A finished reading, ready to show. */
data class ColorReading(
    val rgb: Rgb,
    val name: ColorName,
    val confidence: Confidence,
    /** Smoothed, calibrated color in linear light. */
    val linear: LinearRgb,
    val jitter: Double,
    val calibrated: Boolean,
)

/**
 * Turns per-frame samples into readings: smooths over time, applies the calibration, names the
 * color and rates how far it can be trusted. Holds state, so use one instance per camera session
 * and call it from a single thread.
 */
class ReadingProcessor(window: Int = 8) {

    private val smoother = TemporalSmoother(window)
    private var lastSmoothed: LinearRgb? = null
    private var lastSpread = 0.0

    var calibration: Calibration? = null
        private set

    /** Why the last call to [calibrateFromCurrent] failed; null after a success. */
    var lastCalibrationProblem: CalibrationProblem? = null
        private set

    /** The latest smoothed color before calibration; null until a frame has been processed. */
    val lastMeasured: LinearRgb? get() = lastSmoothed

    fun process(sample: FrameSample): ColorReading {
        val smoothed = smoother.add(sample.mean)
        lastSmoothed = smoothed
        lastSpread = sample.areaSpread
        val active = calibration
        val corrected = active?.apply(smoothed) ?: smoothed
        val rgb = ColorMath.toSrgb(corrected)
        val jitter = smoother.jitter()
        val confidence = ConfidenceRater.rate(
            ReadingQuality(
                sceneLuminance = sample.sceneLuminance,
                clippedFraction = sample.clippedFraction,
                areaSpread = sample.areaSpread,
                temporalJitter = jitter,
                calibrated = active != null,
            ),
        )
        return ColorReading(rgb, ColorNamer.name(rgb), confidence, corrected, jitter, active != null)
    }

    /**
     * Uses the color currently in the ring as the white reference.
     * Returns false when nothing has been measured yet or the reference is unusable.
     */
    fun calibrateFromCurrent(): Boolean {
        val white = lastSmoothed ?: return false
        lastCalibrationProblem = Calibration.problemWith(white, lastSpread)
        val result = Calibration.fromWhiteReference(white, lastSpread) ?: return false
        calibration = result
        return true
    }

    fun clearCalibration() {
        calibration = null
    }

    /** Uses a calibration measured earlier, for example one restored from a saved profile. */
    fun useCalibration(saved: Calibration) {
        calibration = saved
    }

    /** Forgets smoothing history, for example after the camera restarts. Calibration is kept. */
    fun reset() {
        smoother.reset()
        lastSmoothed = null
    }
}
