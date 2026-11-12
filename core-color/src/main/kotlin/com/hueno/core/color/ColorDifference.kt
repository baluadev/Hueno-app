package com.hueno.core.color

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

enum class Verdict(val label: String) {
    SAME("Same"),
    CLOSE("Close"),
    DIFFERENT("Different"),
}

data class Comparison(
    val verdict: Verdict,
    val deltaE: Double,
    val sameFamily: Boolean,
    /** Plain sentence about how B differs from A, for example "B is green and a little lighter". */
    val explanation: String,
)

object ColorDifference {

    /**
     * Verdict thresholds on CIEDE2000. These are starting values; a phone camera is noisier than
     * the lab conditions the textbook thresholds assume, so tune them on real captures.
     */
    const val SAME_BELOW = 3.0
    const val CLOSE_BELOW = 10.0

    /** CIEDE2000 color difference (Sharma, Wu, Dalal 2005), with kL = kC = kH = 1. */
    fun ciede2000(x: Lab, y: Lab): Double {
        val c1 = hypot(x.a, x.b)
        val c2 = hypot(y.a, y.b)
        val cBar = (c1 + c2) / 2.0
        val g = 0.5 * (1 - sqrt(cBar.pow(7) / (cBar.pow(7) + 25.0.pow(7))))
        val a1 = (1 + g) * x.a
        val a2 = (1 + g) * y.a
        val c1p = hypot(a1, x.b)
        val c2p = hypot(a2, y.b)
        val h1p = hueDegrees(x.b, a1)
        val h2p = hueDegrees(y.b, a2)

        val dLp = y.l - x.l
        val dCp = c2p - c1p
        val dhp = when {
            c1p * c2p == 0.0 -> 0.0
            abs(h2p - h1p) <= 180 -> h2p - h1p
            h2p - h1p > 180 -> h2p - h1p - 360
            else -> h2p - h1p + 360
        }
        val dHp = 2 * sqrt(c1p * c2p) * sin(Math.toRadians(dhp / 2))

        val lBarP = (x.l + y.l) / 2
        val cBarP = (c1p + c2p) / 2
        val hBarP = when {
            c1p * c2p == 0.0 -> h1p + h2p
            abs(h1p - h2p) <= 180 -> (h1p + h2p) / 2
            h1p + h2p < 360 -> (h1p + h2p + 360) / 2
            else -> (h1p + h2p - 360) / 2
        }
        val t = 1 -
            0.17 * cos(Math.toRadians(hBarP - 30)) +
            0.24 * cos(Math.toRadians(2 * hBarP)) +
            0.32 * cos(Math.toRadians(3 * hBarP + 6)) -
            0.20 * cos(Math.toRadians(4 * hBarP - 63))
        val dTheta = 30 * exp(-((hBarP - 275) / 25).pow(2))
        val rC = 2 * sqrt(cBarP.pow(7) / (cBarP.pow(7) + 25.0.pow(7)))
        val sL = 1 + 0.015 * (lBarP - 50).pow(2) / sqrt(20 + (lBarP - 50).pow(2))
        val sC = 1 + 0.045 * cBarP
        val sH = 1 + 0.015 * cBarP * t
        val rT = -sin(Math.toRadians(2 * dTheta)) * rC

        val l = dLp / sL
        val c = dCp / sC
        val h = dHp / sH
        return sqrt(l * l + c * c + h * h + rT * c * h)
    }

    private fun hueDegrees(b: Double, a: Double): Double {
        if (a == 0.0 && b == 0.0) return 0.0
        val d = Math.toDegrees(atan2(b, a))
        return if (d < 0) d + 360 else d
    }

    /** Compares color [b] against reference color [a]. */
    fun compare(a: Rgb, b: Rgb): Comparison {
        val deltaE = ciede2000(ColorMath.toLab(a), ColorMath.toLab(b))
        val verdict = when {
            deltaE < SAME_BELOW -> Verdict.SAME
            deltaE < CLOSE_BELOW -> Verdict.CLOSE
            else -> Verdict.DIFFERENT
        }
        val nameA = ColorNamer.name(a)
        val nameB = ColorNamer.name(b)
        val sameFamily = nameA.family == nameB.family
        return Comparison(verdict, deltaE, sameFamily, explain(verdict, nameA, nameB, a, b))
    }

    private fun explain(verdict: Verdict, nameA: ColorName, nameB: ColorName, a: Rgb, b: Rgb): String {
        if (verdict == Verdict.SAME) return "A and B look the same."
        val la = ColorMath.toOklch(a)
        val lb = ColorMath.toOklch(b)
        val parts = mutableListOf<String>()
        val dL = lb.l - la.l
        if (abs(dL) >= 0.04) {
            val amount = if (abs(dL) >= 0.15) "much" else "a little"
            parts += "$amount ${if (dL > 0) "lighter" else "darker"}"
        }
        val dC = lb.c - la.c
        if (abs(dC) >= 0.03) parts += if (dC > 0) "more vivid" else "duller"

        val familyB = nameB.family.label.lowercase()
        return if (nameA.family != nameB.family) {
            val tail = if (parts.isEmpty()) "" else ", and ${parts.joinToString(" and ")}"
            "A is ${nameA.family.label.lowercase()}. B is $familyB$tail."
        } else if (parts.isEmpty()) {
            "Both are $familyB, with a slightly different tint."
        } else {
            "Both are $familyB. B is ${parts.joinToString(" and ")}."
        }
    }
}
