package com.hueno.core.color

/**
 * Per-channel gains measured from a white reference (a sheet of paper).
 * Applied in linear light so that the reference reads as neutral.
 */
data class Calibration(val gainR: Double, val gainG: Double, val gainB: Double) {

    fun apply(c: LinearRgb): LinearRgb = LinearRgb(
        (c.r * gainR).coerceIn(0.0, 1.0),
        (c.g * gainG).coerceIn(0.0, 1.0),
        (c.b * gainB).coerceIn(0.0, 1.0),
    )

    companion object {
        val NONE = Calibration(1.0, 1.0, 1.0)

        /** Below this the reference is too dark to trust; above the upper bound it is clipped. */
        private const val MIN_CHANNEL = 0.05
        private const val MAX_CHANNEL = 0.98

        /** Gains outside this range mean the reference was not white, or the light is extreme. */
        private const val MAX_GAIN_RATIO = 3.0

        /**
         * Where the white reference lands after calibration, in linear light. Paper reflects a
         * bit less than everything, so it maps just under full scale and still reads as "White".
         */
        const val REFERENCE_LEVEL = 0.88

        /**
         * Builds a calibration from the measured color of a white reference.
         *
         * Every later reading is expressed relative to the reference: the gains remove the tint
         * of the light and also fix the brightness scale. That only holds while exposure and
         * white balance stay locked at the values used when the reference was measured.
         *
         * Returns null when the reference is unusable (too dark, clipped, or far from white).
         */
        fun fromWhiteReference(white: LinearRgb): Calibration? {
            val channels = listOf(white.r, white.g, white.b)
            if (channels.any { it < MIN_CHANNEL || it > MAX_CHANNEL }) return null
            if (channels.max() / channels.min() > MAX_GAIN_RATIO) return null
            return Calibration(REFERENCE_LEVEL / white.r, REFERENCE_LEVEL / white.g, REFERENCE_LEVEL / white.b)
        }
    }
}
