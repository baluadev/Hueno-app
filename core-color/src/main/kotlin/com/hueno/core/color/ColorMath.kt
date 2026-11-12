package com.hueno.core.color

import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt

/** An sRGB color with 8-bit channels. */
data class Rgb(val r: Int, val g: Int, val b: Int) {
    init {
        require(r in 0..255 && g in 0..255 && b in 0..255) { "Channel out of range: $r, $g, $b" }
    }

    /** Hex code in the form #RRGGBB. */
    val hex: String get() = "#%02X%02X%02X".format(r, g, b)

    companion object {
        /** Parses "#RRGGBB" or "RRGGBB". */
        fun parse(hex: String): Rgb {
            val s = hex.removePrefix("#")
            require(s.length == 6) { "Expected 6 hex digits, got \"$hex\"" }
            val v = s.toInt(16)
            return Rgb((v shr 16) and 0xFF, (v shr 8) and 0xFF, v and 0xFF)
        }

        /** Builds a color from a packed ARGB int; alpha is ignored. */
        fun fromArgb(argb: Int): Rgb = Rgb((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
    }
}

/** Linear-light RGB, each channel nominally 0..1. Averaging and calibration happen here. */
data class LinearRgb(val r: Double, val g: Double, val b: Double) {
    /** Relative luminance (Rec. 709 weights). */
    val luminance: Double get() = 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/** OKLCH: lightness 0..1, chroma roughly 0..0.4, hue in degrees 0..360. */
data class Oklch(val l: Double, val c: Double, val h: Double)

/** CIE L*a*b* under D65. */
data class Lab(val l: Double, val a: Double, val b: Double)

object ColorMath {

    /** Linear-light value for each 8-bit sRGB channel value. Lets per-pixel loops skip the pow(). */
    val srgbToLinearTable: DoubleArray = DoubleArray(256) { i ->
        val v = i / 255.0
        if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
    }

    private fun srgbToLinear(c: Int): Double = srgbToLinearTable[c]

    private fun linearToSrgb(v: Double): Int {
        val x = v.coerceIn(0.0, 1.0)
        val s = if (x <= 0.0031308) x * 12.92 else 1.055 * x.pow(1 / 2.4) - 0.055
        return (s * 255.0).roundToInt().coerceIn(0, 255)
    }

    fun toLinear(rgb: Rgb): LinearRgb = LinearRgb(srgbToLinear(rgb.r), srgbToLinear(rgb.g), srgbToLinear(rgb.b))

    fun toSrgb(lin: LinearRgb): Rgb = Rgb(linearToSrgb(lin.r), linearToSrgb(lin.g), linearToSrgb(lin.b))

    /** HSL as hue in degrees, saturation and lightness in percent, each rounded to a whole number. */
    fun toHsl(rgb: Rgb): Triple<Int, Int, Int> {
        val r = rgb.r / 255.0
        val g = rgb.g / 255.0
        val b = rgb.b / 255.0
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2
        val d = max - min
        if (d == 0.0) return Triple(0, 0, (l * 100).roundToInt())
        val s = d / (1 - kotlin.math.abs(2 * l - 1))
        val h = when (max) {
            r -> 60 * (((g - b) / d).mod(6.0))
            g -> 60 * ((b - r) / d + 2)
            else -> 60 * ((r - g) / d + 4)
        }
        return Triple(h.roundToInt() % 360, (s * 100).roundToInt(), (l * 100).roundToInt())
    }

    /** OKLab by Björn Ottosson, returned in cylindrical form. */
    fun toOklch(rgb: Rgb): Oklch {
        val lin = toLinear(rgb)
        val l = cbrt(0.4122214708 * lin.r + 0.5363325363 * lin.g + 0.0514459929 * lin.b)
        val m = cbrt(0.2119034982 * lin.r + 0.6806995451 * lin.g + 0.1073969566 * lin.b)
        val s = cbrt(0.0883024619 * lin.r + 0.2817188376 * lin.g + 0.6299787005 * lin.b)
        val okL = 0.2104542553 * l + 0.7936177850 * m - 0.0040720468 * s
        val okA = 1.9779984951 * l - 2.4285922050 * m + 0.4505937099 * s
        val okB = 0.0259040371 * l + 0.7827717662 * m - 0.8086757660 * s
        val chroma = hypot(okA, okB)
        var hue = Math.toDegrees(atan2(okB, okA))
        if (hue < 0) hue += 360.0
        return Oklch(okL, chroma, if (chroma < 1e-6) 0.0 else hue)
    }

    /** sRGB to CIE L*a*b* (D65 white). */
    fun toLab(rgb: Rgb): Lab {
        val lin = toLinear(rgb)
        val x = (0.4124564 * lin.r + 0.3575761 * lin.g + 0.1804375 * lin.b) / 0.95047
        val y = 0.2126729 * lin.r + 0.7151522 * lin.g + 0.0721750 * lin.b
        val z = (0.0193339 * lin.r + 0.1191920 * lin.g + 0.9503041 * lin.b) / 1.08883
        fun f(t: Double) = if (t > 216.0 / 24389.0) cbrt(t) else (24389.0 / 27.0 * t + 16.0) / 116.0
        val fx = f(x)
        val fy = f(y)
        val fz = f(z)
        return Lab(116.0 * fy - 16.0, 500.0 * (fx - fy), 200.0 * (fy - fz))
    }

    /**
     * Average of packed ARGB pixels, computed in linear light.
     * Averaging raw sRGB values would give a result darker than what the eye sees.
     */
    fun averageLinear(argbPixels: IntArray): LinearRgb {
        require(argbPixels.isNotEmpty()) { "No pixels to average" }
        var r = 0.0
        var g = 0.0
        var b = 0.0
        for (p in argbPixels) {
            r += srgbToLinear((p shr 16) and 0xFF)
            g += srgbToLinear((p shr 8) and 0xFF)
            b += srgbToLinear(p and 0xFF)
        }
        val n = argbPixels.size
        return LinearRgb(r / n, g / n, b / n)
    }
}
