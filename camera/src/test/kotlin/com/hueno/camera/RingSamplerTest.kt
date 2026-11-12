package com.hueno.camera

import com.hueno.core.color.ColorMath
import com.hueno.core.color.Rgb
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RingSamplerTest {

    private val width = 64
    private val height = 48

    /** Builds an RGBA frame with row padding, like a real camera buffer. */
    private fun frame(rowPadding: Int = 8, color: (x: Int, y: Int) -> Rgb): Pair<ByteBuffer, Int> {
        val rowStride = width * 4 + rowPadding
        val buffer = ByteBuffer.allocate(rowStride * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                val c = color(x, y)
                val o = y * rowStride + x * 4
                buffer.put(o, c.r.toByte())
                buffer.put(o + 1, c.g.toByte())
                buffer.put(o + 2, c.b.toByte())
                buffer.put(o + 3, 0xFF.toByte())
            }
        }
        return buffer to rowStride
    }

    @Test
    fun uniformFrameReturnsItsColorWithNoSpread() {
        val navy = Rgb.parse("#1F3A5F")
        val (buffer, rowStride) = frame { _, _ -> navy }
        val sample = RingSampler.sample(buffer, width, height, rowStride, 4, SampleSize.LARGE)
        assertNotNull(sample)
        assertEquals(navy, ColorMath.toSrgb(sample!!.mean))
        assertEquals(0.0, sample.areaSpread, 1e-9)
        assertEquals(0.0, sample.clippedFraction, 1e-9)
    }

    @Test
    fun onlyTheCenterIsSampled() {
        val red = Rgb(200, 30, 30)
        val green = Rgb(30, 200, 30)
        // A green frame with a red block in the middle, larger than the ring.
        val (buffer, rowStride) = frame { x, y ->
            if (x in 22..42 && y in 14..34) red else green
        }
        val sample = RingSampler.sample(buffer, width, height, rowStride, 4, SampleSize.LARGE)
        assertEquals(red, ColorMath.toSrgb(sample!!.mean))
        // Scene brightness comes from the whole frame, which is mostly green here.
        val greenLum = ColorMath.toLinear(green).luminance
        val redLum = ColorMath.toLinear(red).luminance
        assertTrue("scene was ${sample.sceneLuminance}", sample.sceneLuminance > (greenLum + redLum) / 2)
    }

    @Test
    fun mixedAreaReportsSpreadAndClipping() {
        // Left half black, right half white: the ring straddles both.
        val (buffer, rowStride) = frame { x, _ -> if (x < width / 2) Rgb(0, 0, 0) else Rgb(255, 255, 255) }
        val sample = RingSampler.sample(buffer, width, height, rowStride, 4, SampleSize.LARGE)!!
        assertTrue("spread was ${sample.areaSpread}", sample.areaSpread > 0.3)
        assertTrue("clipped was ${sample.clippedFraction}", sample.clippedFraction in 0.3..0.7)
    }
}
