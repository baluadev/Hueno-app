package com.hueno.core.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorMathTest {

    @Test
    fun hexRoundTrips() {
        assertEquals("#1F3A5F", Rgb.parse("#1F3A5F").hex)
        assertEquals(Rgb(31, 58, 95), Rgb.parse("1f3a5f"))
        assertEquals(Rgb(31, 58, 95), Rgb.fromArgb(0xFF1F3A5F.toInt()))
    }

    @Test
    fun whiteAndBlackAreNeutralInOklch() {
        val white = ColorMath.toOklch(Rgb(255, 255, 255))
        assertEquals(1.0, white.l, 0.001)
        assertEquals(0.0, white.c, 0.001)
        val black = ColorMath.toOklch(Rgb(0, 0, 0))
        assertEquals(0.0, black.l, 0.001)
    }

    @Test
    fun primaryHuesMatchPublishedOklchValues() {
        // Reference values for sRGB primaries in OKLCH.
        val red = ColorMath.toOklch(Rgb(255, 0, 0))
        assertEquals(0.628, red.l, 0.002)
        assertEquals(0.2577, red.c, 0.002)
        assertEquals(29.23, red.h, 0.1)
        assertEquals(142.5, ColorMath.toOklch(Rgb(0, 255, 0)).h, 0.1)
        assertEquals(264.05, ColorMath.toOklch(Rgb(0, 0, 255)).h, 0.1)
    }

    @Test
    fun hslMatchesKnownValues() {
        assertEquals(Triple(215, 51, 25), ColorMath.toHsl(Rgb.parse("#1F3A5F")))
        assertEquals(Triple(0, 100, 50), ColorMath.toHsl(Rgb(255, 0, 0)))
        assertEquals(Triple(120, 100, 25), ColorMath.toHsl(Rgb(0, 128, 0)))
        assertEquals(Triple(0, 0, 50), ColorMath.toHsl(Rgb(128, 128, 128)))
        assertEquals(Triple(300, 100, 25), ColorMath.toHsl(Rgb(128, 0, 128)))
    }

    @Test
    fun labOfWhiteIsHundredZeroZero() {
        val lab = ColorMath.toLab(Rgb(255, 255, 255))
        assertEquals(100.0, lab.l, 0.01)
        assertEquals(0.0, lab.a, 0.01)
        assertEquals(0.0, lab.b, 0.01)
    }

    @Test
    fun linearRoundTripIsLossless() {
        for (v in listOf(0, 1, 17, 128, 200, 254, 255)) {
            val rgb = Rgb(v, 255 - v, (v * 7) % 256)
            assertEquals(rgb, ColorMath.toSrgb(ColorMath.toLinear(rgb)))
        }
    }

    @Test
    fun averagingHappensInLinearLight() {
        // Half black, half white is a light gray (about 188), not the naive sRGB midpoint 128.
        val avg = ColorMath.toSrgb(ColorMath.averageLinear(intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())))
        assertTrue("expected about 188 but was ${avg.r}", avg.r in 186..189)
        assertEquals(avg.r, avg.g)
        assertEquals(avg.g, avg.b)
    }
}
