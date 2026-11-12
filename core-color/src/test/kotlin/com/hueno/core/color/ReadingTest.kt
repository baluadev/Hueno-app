package com.hueno.core.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ReadingTest {

    @Test
    fun calibrationMakesTheWhiteReferenceNeutral() {
        val warmWhite = LinearRgb(0.80, 0.70, 0.60)
        val calibration = Calibration.fromWhiteReference(warmWhite)
        assertNotNull(calibration)
        val corrected = calibration!!.apply(warmWhite)
        assertEquals(Calibration.REFERENCE_LEVEL, corrected.r, 1e-9)
        assertEquals(Calibration.REFERENCE_LEVEL, corrected.g, 1e-9)
        assertEquals(Calibration.REFERENCE_LEVEL, corrected.b, 1e-9)
        // The reference itself must come out named as white, whatever the light looked like.
        assertEquals(ColorFamily.WHITE, ColorNamer.name(ColorMath.toSrgb(corrected)).family)
    }

    @Test
    fun processorNamesAColorAndRatesIt() {
        val processor = ReadingProcessor(window = 4)
        val navy = ColorMath.toLinear(Rgb.parse("#1F3A5F"))
        var reading = processor.process(FrameSample(navy, clippedFraction = 0.0, areaSpread = 0.01, sceneLuminance = 0.2))
        repeat(3) { reading = processor.process(FrameSample(navy, 0.0, 0.01, 0.2)) }
        assertEquals("Dark blue", reading.name.plain)
        assertEquals("#1F3A5F", reading.rgb.hex)
        // Not calibrated yet, so the rating is capped at Fair.
        assertEquals(Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.NOT_CALIBRATED), reading.confidence)
    }

    @Test
    fun darkObjectInGoodLightIsNotCalledTooDark() {
        val processor = ReadingProcessor(window = 2)
        val black = ColorMath.toLinear(Rgb.parse("#101214"))
        val bright = processor.process(FrameSample(black, 0.0, 0.005, sceneLuminance = 0.2))
        assertEquals(ConfidenceIssue.NOT_CALIBRATED, bright.confidence.issue)
        val dim = processor.process(FrameSample(black, 0.0, 0.005, sceneLuminance = 0.004))
        assertEquals(Confidence(ConfidenceLevel.POOR, ConfidenceIssue.TOO_DARK), dim.confidence)
    }

    @Test
    fun processorCalibratesFromTheCurrentReading() {
        val processor = ReadingProcessor(window = 3)
        assertEquals("nothing measured yet", false, processor.calibrateFromCurrent())

        val warmPaper = LinearRgb(0.70, 0.62, 0.53)
        repeat(3) { processor.process(FrameSample(warmPaper, 0.0, 0.01, 0.2)) }
        assertEquals(true, processor.calibrateFromCurrent())

        val reading = processor.process(FrameSample(warmPaper, 0.0, 0.01, 0.2))
        assertEquals(ColorFamily.WHITE, reading.name.family)
        assertEquals(ConfidenceLevel.GOOD, reading.confidence.level)

        processor.clearCalibration()
        val uncalibrated = processor.process(FrameSample(warmPaper, 0.0, 0.01, 0.2))
        assertEquals(ConfidenceIssue.NOT_CALIBRATED, uncalibrated.confidence.issue)
    }

    @Test
    fun calibrationRejectsUnusableReferences() {
        assertNull("too dark", Calibration.fromWhiteReference(LinearRgb(0.03, 0.03, 0.03)))
        assertNull("clipped", Calibration.fromWhiteReference(LinearRgb(1.0, 1.0, 1.0)))
        assertNull("not white", Calibration.fromWhiteReference(LinearRgb(0.8, 0.2, 0.1)))
    }

    /** Values logged on a Galaxy Z Flip4, 12/11/2026. */
    @Test
    fun calibrationCheckMatchesRealDeviceMeasurements() {
        // White paper in room light, and close to a yellow lamp.
        assertNotNull(Calibration.fromWhiteReference(LinearRgb(0.513, 0.532, 0.577), areaSpread = 0.009))
        assertNotNull(Calibration.fromWhiteReference(LinearRgb(0.494, 0.490, 0.490), areaSpread = 0.005))
        assertNotNull(Calibration.fromWhiteReference(LinearRgb(0.914, 0.784, 0.667), areaSpread = 0.008))
        // White paper right under the lamp: the red channel clipped.
        assertEquals(
            CalibrationProblem.TOO_BRIGHT,
            Calibration.problemWith(LinearRgb(1.000, 0.915, 0.765), areaSpread = 0.017),
        )
        // Pale blue mug, even and filling the ring, after white balance settled on it.
        assertNull(Calibration.fromWhiteReference(LinearRgb(0.235, 0.306, 0.433), areaSpread = 0.017))
        // The same mug measured under white balance held from the wall: colored and uneven.
        assertNull(Calibration.fromWhiteReference(LinearRgb(0.114, 0.160, 0.244), areaSpread = 0.082))
        // White remote control with print: neutral enough, but uneven.
        assertEquals(
            CalibrationProblem.UNEVEN,
            Calibration.problemWith(LinearRgb(0.553, 0.592, 0.643), areaSpread = 0.141),
        )
        // Wood.
        assertNull(Calibration.fromWhiteReference(LinearRgb(0.515, 0.236, 0.038), areaSpread = 0.041))
    }

    @Test
    fun processorRejectsAnUnevenReference() {
        val processor = ReadingProcessor(window = 2)
        val paper = LinearRgb(0.55, 0.55, 0.55)
        repeat(2) { processor.process(FrameSample(paper, 0.0, areaSpread = 0.15, sceneLuminance = 0.3)) }
        assertEquals(false, processor.calibrateFromCurrent())
        processor.process(FrameSample(paper, 0.0, areaSpread = 0.01, sceneLuminance = 0.3))
        assertEquals(true, processor.calibrateFromCurrent())
    }

    @Test
    fun noCalibrationLeavesColorsUnchanged() {
        val c = LinearRgb(0.2, 0.4, 0.6)
        assertEquals(c, Calibration.NONE.apply(c))
    }

    private fun quality(
        sceneLuminance: Double = 0.2,
        clipped: Double = 0.0,
        spread: Double = 0.01,
        jitter: Double = 0.005,
        calibrated: Boolean = true,
    ) = ReadingQuality(sceneLuminance, clipped, spread, jitter, calibrated)

    @Test
    fun confidenceLevels() {
        assertEquals(Confidence(ConfidenceLevel.GOOD, null), ConfidenceRater.rate(quality()))
        assertEquals(
            Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.NOT_CALIBRATED),
            ConfidenceRater.rate(quality(calibrated = false)),
        )
        assertEquals(
            Confidence(ConfidenceLevel.POOR, ConfidenceIssue.TOO_DARK),
            ConfidenceRater.rate(quality(sceneLuminance = 0.005)),
        )
        assertEquals(
            Confidence(ConfidenceLevel.POOR, ConfidenceIssue.TOO_BRIGHT),
            ConfidenceRater.rate(quality(clipped = 0.5)),
        )
        assertEquals(
            Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.MIXED_AREA),
            ConfidenceRater.rate(quality(spread = 0.2)),
        )
        assertEquals(
            Confidence(ConfidenceLevel.POOR, ConfidenceIssue.UNSTEADY),
            ConfidenceRater.rate(quality(jitter = 0.2)),
        )
    }

    @Test
    fun smootherIgnoresASingleOutlierFrame() {
        val smoother = TemporalSmoother(window = 5)
        val steady = LinearRgb(0.2, 0.3, 0.4)
        repeat(4) { smoother.add(steady) }
        val result = smoother.add(LinearRgb(0.9, 0.9, 0.9))
        assertEquals(steady, result)
    }

    @Test
    fun smootherReportsJitterAndForgetsOldFrames() {
        val smoother = TemporalSmoother(window = 3)
        smoother.add(LinearRgb(0.9, 0.9, 0.9))
        repeat(3) { smoother.add(LinearRgb(0.2, 0.2, 0.2)) }
        assertEquals(0.0, smoother.jitter(), 1e-9)
        smoother.add(LinearRgb(0.3, 0.3, 0.3))
        assertEquals(0.1, smoother.jitter(), 1e-9)
    }
}
