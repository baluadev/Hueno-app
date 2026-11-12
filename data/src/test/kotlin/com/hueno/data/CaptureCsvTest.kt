package com.hueno.data

import com.hueno.core.color.Confidence
import com.hueno.core.color.ConfidenceLevel
import com.hueno.core.color.Rgb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CaptureCsvTest {

    @Test
    fun writesHeaderAndOneRowPerColor() {
        val csv = CaptureCsv.build(
            listOf(
                StoredCapture(
                    id = 1,
                    rgb = Rgb.parse("#1F3A5F"),
                    fromPhoto = false,
                    confidence = Confidence(ConfidenceLevel.GOOD, null),
                    calibrated = true,
                    timeMillis = 0,
                    label = "Front door",
                    collection = "House",
                ),
                StoredCapture(2, Rgb.parse("#FF0000"), true, null, false, 1_000),
            ),
        )
        val lines = csv.split("\r\n")
        assertEquals(4, lines.size) // header, two rows, trailing empty piece
        assertTrue(lines[0].startsWith("custom_name,collection,plain_name,detail_name,hex,"))
        assertEquals(
            "Front door,House,Dark blue,Midnight blue,#1F3A5F,31,58,95,215,51,25,camera,good,true,1970-01-01T00:00:00Z",
            lines[1],
        )
        assertEquals(",,Red,Red,#FF0000,255,0,0,0,100,50,photo,,,1970-01-01T00:00:01Z", lines[2])
        assertEquals("", lines[3])
    }

    @Test
    fun quotesFieldsThatNeedIt() {
        assertEquals("plain", CaptureCsv.field("plain"))
        assertEquals("\"wall, north\"", CaptureCsv.field("wall, north"))
        assertEquals("\"the \"\"good\"\" red\"", CaptureCsv.field("the \"good\" red"))
        assertEquals("", CaptureCsv.field(""))
    }

    @Test
    fun neutralizesSpreadsheetFormulas() {
        assertEquals("'=SUM(A1)", CaptureCsv.field("=SUM(A1)"))
        assertEquals("'+1", CaptureCsv.field("+1"))
        assertEquals("'-1", CaptureCsv.field("-1"))
        assertEquals("'@cmd", CaptureCsv.field("@cmd"))
        // A hex code starts with # and is left alone.
        assertEquals("#FF0000", CaptureCsv.field("#FF0000"))
    }

    @Test
    fun cleaningNamesTrimsAndCaps() {
        assertEquals("Living room", CaptureMapper.clean("  Living \n room  "))
        assertNull(CaptureMapper.clean("   "))
        assertNull(CaptureMapper.clean(null))
        assertEquals(CaptureMapper.MAX_TEXT, CaptureMapper.clean("x".repeat(200))!!.length)
    }
}
