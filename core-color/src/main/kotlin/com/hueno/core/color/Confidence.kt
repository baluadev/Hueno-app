package com.hueno.core.color

enum class ConfidenceLevel(val label: String) {
    GOOD("Good"),
    FAIR("Fair"),
    POOR("Poor"),
}

enum class ConfidenceIssue(val tip: String) {
    TOO_DARK("Too dark, add light"),
    TOO_BRIGHT("Too bright, reduce glare"),
    UNSTEADY("Hold the phone steady"),
    MIXED_AREA("More than one color in the ring"),
    NOT_CALIBRATED("Calibrate with white paper for a better reading"),
}

data class Confidence(val level: ConfidenceLevel, val issue: ConfidenceIssue?)

/** What the camera pipeline measured about one reading. All values are in linear light, 0..1. */
data class ReadingQuality(
    /**
     * Mean luminance of the whole frame. Darkness is judged on the scene, not on the ring:
     * a navy object in good light is dark inside the ring but the reading is still sound.
     */
    val sceneLuminance: Double,
    /** Share of sampled pixels with a channel at the top of the range. */
    val clippedFraction: Double,
    /** Spread of colors inside the ring (standard deviation of luminance). */
    val areaSpread: Double,
    /** Frame-to-frame movement of the averaged color over the smoothing window. */
    val temporalJitter: Double,
    val calibrated: Boolean,
)

/**
 * Decides how far a reading can be trusted. The thresholds are first-draft values to be tuned
 * on real devices; an uncalibrated reading is never rated better than Fair.
 */
object ConfidenceRater {

    fun rate(q: ReadingQuality): Confidence {
        // Hard failures first: the reading is not usable.
        if (q.sceneLuminance < 0.012) return Confidence(ConfidenceLevel.POOR, ConfidenceIssue.TOO_DARK)
        if (q.clippedFraction > 0.25) return Confidence(ConfidenceLevel.POOR, ConfidenceIssue.TOO_BRIGHT)
        if (q.temporalJitter > 0.08) return Confidence(ConfidenceLevel.POOR, ConfidenceIssue.UNSTEADY)

        // Softer problems cap the rating at Fair.
        if (q.sceneLuminance < 0.035) return Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.TOO_DARK)
        if (q.clippedFraction > 0.05) return Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.TOO_BRIGHT)
        if (q.areaSpread > 0.10) return Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.MIXED_AREA)
        if (q.temporalJitter > 0.03) return Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.UNSTEADY)
        if (!q.calibrated) return Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.NOT_CALIBRATED)

        return Confidence(ConfidenceLevel.GOOD, null)
    }
}
