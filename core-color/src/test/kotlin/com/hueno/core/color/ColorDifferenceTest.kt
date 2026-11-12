package com.hueno.core.color

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorDifferenceTest {

    @Test
    fun ciede2000MatchesPublishedTestPairs() {
        // Pairs from the Sharma, Wu and Dalal (2005) CIEDE2000 test data.
        val cases = listOf(
            Triple(Lab(50.0, 2.6772, -79.7751), Lab(50.0, 0.0, -82.7485), 2.0425),
            Triple(Lab(50.0, 3.1571, -77.2803), Lab(50.0, 0.0, -82.7485), 2.8615),
            Triple(Lab(50.0, 2.5, 0.0), Lab(50.0, 0.0, -2.5), 4.3065),
            Triple(Lab(50.0, 2.5, 0.0), Lab(73.0, 25.0, -18.0), 27.1492),
            Triple(Lab(60.2574, -34.0099, 36.2677), Lab(60.4626, -34.1751, 39.4387), 1.2644),
        )
        for ((a, b, expected) in cases) {
            assertEquals(expected, ColorDifference.ciede2000(a, b), 0.0001)
            assertEquals(expected, ColorDifference.ciede2000(b, a), 0.0001)
        }
    }

    @Test
    fun identicalColorsAreSame() {
        val c = Rgb.parse("#1F3A5F")
        val result = ColorDifference.compare(c, c)
        assertEquals(Verdict.SAME, result.verdict)
        assertEquals(0.0, result.deltaE, 1e-9)
        assertTrue(result.sameFamily)
    }

    @Test
    fun redAndGreenAreDifferentAndExplained() {
        val result = ColorDifference.compare(Rgb.parse("#B5362A"), Rgb.parse("#3E9B63"))
        assertEquals(Verdict.DIFFERENT, result.verdict)
        assertFalse(result.sameFamily)
        assertTrue(result.explanation, result.explanation.startsWith("A is red. B is green"))
        assertTrue(result.explanation, result.explanation.contains("lighter"))
    }

    @Test
    fun slightlyDifferentShadesAreClose() {
        val result = ColorDifference.compare(Rgb.parse("#1F3A5F"), Rgb.parse("#27456F"))
        assertEquals(Verdict.CLOSE, result.verdict)
        assertTrue(result.sameFamily)
    }
}
