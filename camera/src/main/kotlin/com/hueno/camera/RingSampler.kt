package com.hueno.camera

import com.hueno.core.color.ColorMath
import com.hueno.core.color.FrameSample
import com.hueno.core.color.LinearRgb
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/** How much of the frame the sampling ring covers, as a share of the shorter side. */
enum class SampleSize(val radiusFraction: Double) {
    SMALL(0.02),
    MEDIUM(0.05),
    LARGE(0.10),
}

/**
 * Averages the pixels inside a centered circle of an RGBA_8888 frame.
 * The circle is centered, so the result does not depend on how the frame is rotated.
 */
internal object RingSampler {

    /** A channel at or above this value counts as clipped. */
    private const val CLIP_LEVEL = 250

    /** Upper bound on pixels read per frame; larger rings are subsampled on a regular grid. */
    private const val MAX_SAMPLES = 4096

    /** Scene brightness is read on a grid with about this many points along the shorter side. */
    private const val SCENE_GRID = 24

    fun sample(
        buffer: ByteBuffer,
        width: Int,
        height: Int,
        rowStride: Int,
        pixelStride: Int,
        size: SampleSize,
    ): FrameSample? {
        if (width <= 0 || height <= 0) return null
        val radius = max(1, (min(width, height) * size.radiusFraction).toInt())
        val cx = width / 2
        val cy = height / 2
        val r2 = radius * radius
        val area = Math.PI * r2
        val step = max(1, sqrt(area / MAX_SAMPLES).toInt())
        val table = ColorMath.srgbToLinearTable

        var sumR = 0.0
        var sumG = 0.0
        var sumB = 0.0
        var sumLum = 0.0
        var sumLum2 = 0.0
        var clipped = 0
        var count = 0

        var dy = -radius
        while (dy <= radius) {
            val y = cy + dy
            if (y in 0 until height) {
                var dx = -radius
                while (dx <= radius) {
                    val x = cx + dx
                    if (x in 0 until width && dx * dx + dy * dy <= r2) {
                        val offset = y * rowStride + x * pixelStride
                        val r8 = buffer.get(offset).toInt() and 0xFF
                        val g8 = buffer.get(offset + 1).toInt() and 0xFF
                        val b8 = buffer.get(offset + 2).toInt() and 0xFF
                        val r = table[r8]
                        val g = table[g8]
                        val b = table[b8]
                        sumR += r
                        sumG += g
                        sumB += b
                        val lum = 0.2126 * r + 0.7152 * g + 0.0722 * b
                        sumLum += lum
                        sumLum2 += lum * lum
                        if (r8 >= CLIP_LEVEL || g8 >= CLIP_LEVEL || b8 >= CLIP_LEVEL) clipped++
                        count++
                    }
                    dx += step
                }
            }
            dy += step
        }
        if (count == 0) return null

        // Coarse grid over the whole frame, to tell a dark object from a dark scene.
        val gridStep = max(1, min(width, height) / SCENE_GRID)
        var sceneSum = 0.0
        var sceneCount = 0
        var gy = gridStep / 2
        while (gy < height) {
            var gx = gridStep / 2
            while (gx < width) {
                val offset = gy * rowStride + gx * pixelStride
                sceneSum += 0.2126 * table[buffer.get(offset).toInt() and 0xFF] +
                    0.7152 * table[buffer.get(offset + 1).toInt() and 0xFF] +
                    0.0722 * table[buffer.get(offset + 2).toInt() and 0xFF]
                sceneCount++
                gx += gridStep
            }
            gy += gridStep
        }

        val meanLum = sumLum / count
        val variance = max(0.0, sumLum2 / count - meanLum * meanLum)
        return FrameSample(
            mean = LinearRgb(sumR / count, sumG / count, sumB / count),
            clippedFraction = clipped.toDouble() / count,
            areaSpread = sqrt(variance),
            sceneLuminance = if (sceneCount == 0) meanLum else sceneSum / sceneCount,
        )
    }
}
