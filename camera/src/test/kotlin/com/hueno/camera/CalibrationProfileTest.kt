package com.hueno.camera

import com.hueno.core.color.Calibration
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalibrationProfileTest {

    private val identity = listOf(1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1)

    private fun profile(
        name: String = "Living room",
        exposure: Long = 10_000_000,
        iso: Int = 200,
        gains: List<Float> = listOf(2f, 1f, 1f, 1.5f),
        transform: List<Int> = identity,
        calibration: Calibration = Calibration(1.1, 1.0, 1.4),
    ) = CalibrationProfile("id", name, calibration, exposure, iso, gains, transform)

    @Test
    fun aCompleteProfileIsUsable() {
        assertTrue(profile().isUsable())
    }

    @Test
    fun brokenProfilesAreRejected() {
        assertFalse("blank name", profile(name = " ").isUsable())
        assertFalse("no exposure", profile(exposure = 0).isUsable())
        assertFalse("no sensitivity", profile(iso = 0).isUsable())
        assertFalse("three gains", profile(gains = listOf(1f, 1f, 1f)).isUsable())
        assertFalse("negative gain", profile(gains = listOf(1f, -1f, 1f, 1f)).isUsable())
        assertFalse("short matrix", profile(transform = identity.take(16)).isUsable())
        assertFalse("zero denominator", profile(transform = identity.toMutableList().also { it[1] = 0 }).isUsable())
        assertFalse("bad calibration", profile(calibration = Calibration(Double.NaN, 1.0, 1.0)).isUsable())
    }
}
