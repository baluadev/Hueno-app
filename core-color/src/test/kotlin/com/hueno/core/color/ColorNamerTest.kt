package com.hueno.core.color

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorNamerTest {

    /** Colors whose plain name most people would agree on. Expectations come from that, not from the code. */
    private val golden = listOf(
        "#000000" to "Black",
        "#FFFFFF" to "White",
        "#808080" to "Gray",
        "#D9D4C7" to "Light gray",
        "#3A3A3A" to "Dark gray",
        "#FF0000" to "Red",
        "#B5362A" to "Red",
        "#FFA500" to "Orange",
        "#FFFF00" to "Yellow",
        "#C9A227" to "Dark yellow",
        "#008000" to "Green",
        "#3E9B63" to "Green",
        "#90EE90" to "Light green",
        "#006400" to "Dark green",
        "#008080" to "Blue-green",
        "#0000FF" to "Blue",
        "#1F3A5F" to "Dark blue",
        "#000080" to "Dark blue",
        "#87CEEB" to "Light blue",
        "#800080" to "Purple",
        "#FFC0CB" to "Light pink",
        "#FF69B4" to "Pink",
        "#8B4513" to "Reddish brown",
        "#8B4A2B" to "Reddish brown",
        "#5C4033" to "Dark brown",
        "#800000" to "Dark red",
        "#4A2C2A" to "Dark brown",
        "#704241" to "Brown",
        // A red pom-pom read without calibration on a Galaxy Z Flip4, once called "Dark brown".
        "#4E0E08" to "Dark red",
        "#581F14" to "Dark red",
    )

    @Test
    fun plainNamesMatchCommonUsage() {
        val wrong = golden.mapNotNull { (hex, expected) ->
            val actual = ColorNamer.name(Rgb.parse(hex)).plain
            if (actual == expected) null else "$hex expected \"$expected\" but was \"$actual\" ${ColorMath.toOklch(Rgb.parse(hex))}"
        }
        assertEquals(wrong.joinToString("\n"), 0, wrong.size)
    }

    @Test
    fun darkNeutralsAreNotGivenAHueName() {
        // A regression seen in other apps: dark, dull colors all reported under one odd name.
        for (hex in listOf("#1A1A1C", "#101214", "#0B0B0B")) {
            assertEquals(hex, ColorFamily.BLACK, ColorNamer.name(Rgb.parse(hex)).family)
        }
    }

    @Test
    fun whitePaperIsNotCalledGray() {
        assertEquals(ColorFamily.WHITE, ColorNamer.name(Rgb.parse("#FAFAF7")).family)
    }

    @Test
    fun detailNameStaysInTheSameFamilyAsThePlainName() {
        // Seen on the emulator: a tan wall named "Brown" with the detail "Gray".
        for (hex in golden.map { it.first } + "#8E7963") {
            val rgb = Rgb.parse(hex)
            val name = ColorNamer.name(rgb)
            val detailRgb = NamedColors.all.first { it.name == name.detail }.rgb
            assertEquals("$hex ${name.plain} / ${name.detail}", name.family, ColorNamer.name(detailRgb).family)
        }
    }

    @Test
    fun detailNameIsNearestNamedColor() {
        assertEquals("Navy", ColorNamer.name(Rgb.parse("#000080")).detail)
        assertEquals("Sea green", ColorNamer.name(Rgb.parse("#2E8B57")).detail)
    }
}
