package com.hueno.data

import android.content.Context
import com.hueno.core.color.Confidence
import com.hueno.core.color.ConfidenceIssue
import com.hueno.core.color.ConfidenceLevel
import com.hueno.core.color.Rgb
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A color kept in history. */
data class StoredCapture(
    val id: Long,
    val rgb: Rgb,
    val fromPhoto: Boolean,
    /** Confidence when it was captured; null for photo picks, which are exact pixel values. */
    val confidence: Confidence?,
    val calibrated: Boolean,
    val timeMillis: Long,
    /** A name the user gave this color; null when they gave none. */
    val label: String? = null,
    /** The collection it is filed under; null when it is in none. */
    val collection: String? = null,
)

/** Saved colors, newest first. */
class CaptureRepository internal constructor(private val dao: CaptureDao) {

    constructor(context: Context) : this(HuenoDatabase.get(context).captures())

    fun observeAll(): Flow<List<StoredCapture>> = dao.observeAll().map { rows -> rows.map(CaptureMapper::toStored) }

    /** Saves a color and returns it with its new id. */
    suspend fun add(
        rgb: Rgb,
        fromPhoto: Boolean,
        confidence: Confidence?,
        calibrated: Boolean,
        timeMillis: Long = System.currentTimeMillis(),
    ): StoredCapture {
        val entity = CaptureMapper.toEntity(rgb, fromPhoto, confidence, calibrated, timeMillis)
        val id = dao.insert(entity)
        return CaptureMapper.toStored(entity.copy(id = id))
    }

    suspend fun delete(id: Long) = dao.delete(id)

    /** Sets the user's own name for a color. Blank text clears it. */
    suspend fun rename(id: Long, label: String?) = dao.setLabel(id, CaptureMapper.clean(label))

    /** Files a color under a collection. Blank text takes it out of any collection. */
    suspend fun moveToCollection(id: Long, collection: String?) = dao.setCollection(id, CaptureMapper.clean(collection))
}

internal object CaptureMapper {

    /** Longest name or collection title kept. */
    const val MAX_TEXT = 60

    /** Trims, collapses line breaks and caps the length; returns null for blank text. */
    fun clean(text: String?): String? =
        text?.replace(Regex("\\s+"), " ")?.trim()?.take(MAX_TEXT)?.trim()?.takeIf { it.isNotEmpty() }

    fun toEntity(rgb: Rgb, fromPhoto: Boolean, confidence: Confidence?, calibrated: Boolean, timeMillis: Long) =
        CaptureEntity(
            red = rgb.r,
            green = rgb.g,
            blue = rgb.b,
            fromPhoto = fromPhoto,
            confidenceLevel = confidence?.level?.name,
            confidenceIssue = confidence?.issue?.name,
            calibrated = calibrated,
            timeMillis = timeMillis,
        )

    fun toStored(e: CaptureEntity): StoredCapture {
        // Values written by a newer or older version of the app may not be known here.
        // An unknown level drops the confidence; an unknown issue keeps the level without it.
        val level = e.confidenceLevel?.let { name -> ConfidenceLevel.entries.firstOrNull { it.name == name } }
        val issue = e.confidenceIssue?.let { name -> ConfidenceIssue.entries.firstOrNull { it.name == name } }
        return StoredCapture(
            id = e.id,
            rgb = Rgb(e.red.coerceIn(0, 255), e.green.coerceIn(0, 255), e.blue.coerceIn(0, 255)),
            fromPhoto = e.fromPhoto,
            confidence = level?.let { Confidence(it, issue) },
            calibrated = e.calibrated,
            timeMillis = e.timeMillis,
            label = clean(e.label),
            collection = clean(e.collection),
        )
    }
}
