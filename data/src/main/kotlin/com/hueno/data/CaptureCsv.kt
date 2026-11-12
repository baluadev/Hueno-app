package com.hueno.data

import com.hueno.core.color.ColorMath
import com.hueno.core.color.ColorNamer
import java.time.Instant

/** Builds a CSV file of saved colors that opens cleanly in spreadsheet apps. */
object CaptureCsv {

    private val HEADER = listOf(
        "custom_name", "collection", "plain_name", "detail_name", "hex",
        "red", "green", "blue", "hue", "saturation", "lightness",
        "source", "confidence", "calibrated", "captured_at_utc",
    )

    /** Rows are separated with CRLF, as RFC 4180 specifies. */
    fun build(captures: List<StoredCapture>): String = buildString {
        append(HEADER.joinToString(","))
        append("\r\n")
        for (c in captures) {
            val name = ColorNamer.name(c.rgb)
            val (h, s, l) = ColorMath.toHsl(c.rgb)
            val row = listOf(
                c.label.orEmpty(),
                c.collection.orEmpty(),
                name.plain,
                name.detail,
                c.rgb.hex,
                c.rgb.r.toString(),
                c.rgb.g.toString(),
                c.rgb.b.toString(),
                h.toString(),
                s.toString(),
                l.toString(),
                if (c.fromPhoto) "photo" else "camera",
                c.confidence?.level?.name?.lowercase().orEmpty(),
                if (c.fromPhoto) "" else c.calibrated.toString(),
                Instant.ofEpochMilli(c.timeMillis).toString(),
            )
            append(row.joinToString(",") { field(it) })
            append("\r\n")
        }
    }

    /**
     * Escapes one field. Text the user typed could start with a character that spreadsheet apps
     * treat as a formula, so those get a leading apostrophe and are shown as plain text.
     */
    internal fun field(raw: String): String {
        val safe = if (raw.isNotEmpty() && raw[0] in FORMULA_STARTS) "'$raw" else raw
        val needsQuotes = safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + safe.replace("\"", "\"\"") + "\"" else safe
    }

    private val FORMULA_STARTS = setOf('=', '+', '-', '@', '\t')
}
