package com.hueno.core.color

import kotlin.math.abs

/**
 * Keeps the last few averaged readings and returns their per-channel median, so the color name
 * does not flicker from frame to frame. Not thread-safe: use it from the analysis thread only.
 */
class TemporalSmoother(private val window: Int = 8) {

    init {
        require(window >= 1) { "Window must be at least 1" }
    }

    private val samples = ArrayDeque<LinearRgb>()

    fun reset() = samples.clear()

    /** Adds a reading and returns the smoothed color. */
    fun add(sample: LinearRgb): LinearRgb {
        samples.addLast(sample)
        while (samples.size > window) samples.removeFirst()
        return LinearRgb(median { it.r }, median { it.g }, median { it.b })
    }

    /** Largest luminance step between consecutive readings in the window; 0 with fewer than two. */
    fun jitter(): Double {
        var worst = 0.0
        for (i in 1 until samples.size) {
            worst = maxOf(worst, abs(samples[i].luminance - samples[i - 1].luminance))
        }
        return worst
    }

    private inline fun median(channel: (LinearRgb) -> Double): Double {
        val sorted = samples.map(channel).sorted()
        val mid = sorted.size / 2
        return if (sorted.size % 2 == 1) sorted[mid] else (sorted[mid - 1] + sorted[mid]) / 2.0
    }
}
