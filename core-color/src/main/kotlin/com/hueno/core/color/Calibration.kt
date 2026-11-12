package com.hueno.core.color

/** Why a surface cannot serve as the white reference. Each one needs a different fix. */
enum class CalibrationProblem {
    /** Too little light on the surface. */
    TOO_DARK,
    /** A channel is at the top of the range, so the true color is lost. */
    TOO_BRIGHT,
    /** Clearly colored, or the light is too strongly tinted. */
    NOT_WHITE,
    /** Print, texture, an edge or several objects in the ring. */
    UNEVEN,
}

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

        /**
         * Strongest over weakest channel of the reference. Above this it was not white, or the
         * light is extreme. Measured on a Galaxy Z Flip4, with automatic white balance settled on
         * the surface before locking: white paper 1.01 to 1.13 in room light and up to 1.37
         * close to a yellow lamp; a pale blue mug 1.85 to 1.90; wood 13.6. The limit sits about
         * midway between paper under the lamp and the mug. A light gray wall measured 1.08, so
         * no ratio can tell it from paper.
         */
        const val MAX_GAIN_RATIO = 1.6

        /**
         * Spread of brightness across the ring, relative to its mean. Paper and a plain wall
         * measured about 0.02; a white remote with print 0.24, a mug 0.53, wood 0.15. A high
         * value means the ring held print, texture, an edge or several objects.
         */
        const val MAX_RELATIVE_SPREAD = 0.08

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
         * Returns null when the reference is unusable: too dark, clipped, far from white, or
         * uneven. [areaSpread] is the standard deviation of luminance across the ring.
         *
         * A light gray or cream surface passes these checks, because the camera's automatic white
         * balance and exposure make it look like paper. The app has to ask for real white paper.
         */
        fun fromWhiteReference(white: LinearRgb, areaSpread: Double = 0.0): Calibration? {
            if (problemWith(white, areaSpread) != null) return null
            return Calibration(REFERENCE_LEVEL / white.r, REFERENCE_LEVEL / white.g, REFERENCE_LEVEL / white.b)
        }

        /** The reason [white] cannot be used as the reference, or null when it can. */
        fun problemWith(white: LinearRgb, areaSpread: Double = 0.0): CalibrationProblem? {
            val channels = listOf(white.r, white.g, white.b)
            return when {
                // Brightness first: a clipped or dark reading also distorts the color ratio.
                channels.any { it > MAX_CHANNEL } -> CalibrationProblem.TOO_BRIGHT
                channels.any { it < MIN_CHANNEL } -> CalibrationProblem.TOO_DARK
                areaSpread / white.luminance > MAX_RELATIVE_SPREAD -> CalibrationProblem.UNEVEN
                channels.max() / channels.min() > MAX_GAIN_RATIO -> CalibrationProblem.NOT_WHITE
                else -> null
            }
        }
    }
}
