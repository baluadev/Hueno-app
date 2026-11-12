package com.hueno.data

import com.hueno.core.color.Confidence
import com.hueno.core.color.ConfidenceIssue
import com.hueno.core.color.ConfidenceLevel
import com.hueno.core.color.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CaptureMapperTest {

    @Test
    fun cameraCaptureRoundTrips() {
        val confidence = Confidence(ConfidenceLevel.FAIR, ConfidenceIssue.NOT_CALIBRATED)
        val entity = CaptureMapper.toEntity(Rgb.parse("#1F3A5F"), false, confidence, calibrated = true, timeMillis = 42)
        val stored = CaptureMapper.toStored(entity.copy(id = 7))
        assertEquals(StoredCapture(7, Rgb.parse("#1F3A5F"), false, confidence, true, 42), stored)
    }

    @Test
    fun photoPickHasNoConfidence() {
        val entity = CaptureMapper.toEntity(Rgb(1, 2, 3), true, null, calibrated = false, timeMillis = 1)
        assertNull(entity.confidenceLevel)
        assertNull(CaptureMapper.toStored(entity).confidence)
    }

    @Test
    fun goodReadingKeepsLevelWithoutIssue() {
        val entity = CaptureMapper.toEntity(Rgb(9, 9, 9), false, Confidence(ConfidenceLevel.GOOD, null), true, 1)
        assertEquals(Confidence(ConfidenceLevel.GOOD, null), CaptureMapper.toStored(entity).confidence)
    }

    @Test
    fun unknownStoredValuesDoNotCrash() {
        val base = CaptureMapper.toEntity(Rgb(9, 9, 9), false, null, false, 1)
        val unknownLevel = base.copy(confidenceLevel = "EXCELLENT", confidenceIssue = "TOO_DARK")
        assertNull(CaptureMapper.toStored(unknownLevel).confidence)

        val unknownIssue = base.copy(confidenceLevel = "FAIR", confidenceIssue = "LENS_DIRTY")
        assertEquals(Confidence(ConfidenceLevel.FAIR, null), CaptureMapper.toStored(unknownIssue).confidence)

        val outOfRange = base.copy(red = 999, green = -5)
        assertEquals(Rgb(255, 0, 9), CaptureMapper.toStored(outOfRange).rgb)
    }
}
