package com.hueno.camera

import android.content.Context
import com.hueno.core.color.Calibration
import org.json.JSONArray
import org.json.JSONObject

/**
 * A calibration saved for one place, such as "Living room".
 *
 * A calibration is only valid for the exposure and white balance it was measured with, so a
 * profile stores those camera settings too. Using it puts the camera back into exactly that
 * state; it is right for the same light, not for a different one.
 */
data class CalibrationProfile(
    val id: String,
    val name: String,
    val calibration: Calibration,
    /** Sensor exposure time in nanoseconds. */
    val exposureNanos: Long,
    /** Sensor sensitivity (ISO). */
    val sensitivity: Int,
    /** White-balance gains for red, green (even), green (odd) and blue. */
    val colorGains: List<Float>,
    /** The 3 x 3 color correction matrix as nine numerator and denominator pairs. */
    val colorTransform: List<Int>,
) {
    internal fun toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("gainR", calibration.gainR)
        .put("gainG", calibration.gainG)
        .put("gainB", calibration.gainB)
        .put("exposureNanos", exposureNanos)
        .put("sensitivity", sensitivity)
        .put("colorGains", JSONArray(colorGains.map { it.toDouble() }))
        .put("colorTransform", JSONArray(colorTransform))

    internal companion object {
        /** Returns null for an entry that is incomplete or out of range, so bad data is skipped. */
        fun fromJson(json: JSONObject): CalibrationProfile? = try {
            val gains = json.getJSONArray("colorGains")
            val transform = json.getJSONArray("colorTransform")
            val profile = CalibrationProfile(
                id = json.getString("id"),
                name = json.getString("name"),
                calibration = Calibration(json.getDouble("gainR"), json.getDouble("gainG"), json.getDouble("gainB")),
                exposureNanos = json.getLong("exposureNanos"),
                sensitivity = json.getInt("sensitivity"),
                colorGains = List(gains.length()) { gains.getDouble(it).toFloat() },
                colorTransform = List(transform.length()) { transform.getInt(it) },
            )
            profile.takeIf { it.isUsable() }
        } catch (e: Exception) {
            null
        }
    }

    internal fun isUsable(): Boolean =
        name.isNotBlank() &&
            exposureNanos > 0 && sensitivity > 0 &&
            colorGains.size == 4 && colorGains.all { it.isFinite() && it > 0f } &&
            colorTransform.size == 18 && (0 until 9).all { colorTransform[it * 2 + 1] != 0 } &&
            listOf(calibration.gainR, calibration.gainG, calibration.gainB).all { it.isFinite() && it > 0.0 }
}

/** Saved calibration profiles, kept on the device. */
class CalibrationProfileStore(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("hueno_calibration", Context.MODE_PRIVATE)

    fun all(): List<CalibrationProfile> = try {
        val array = JSONArray(prefs.getString(KEY, "[]"))
        (0 until array.length()).mapNotNull { CalibrationProfile.fromJson(array.getJSONObject(it)) }
    } catch (e: Exception) {
        emptyList()
    }

    /** Adds [profile], replacing any saved one with the same id. Returns the new list. */
    fun save(profile: CalibrationProfile): List<CalibrationProfile> =
        write(all().filterNot { it.id == profile.id } + profile)

    fun delete(id: String): List<CalibrationProfile> = write(all().filterNot { it.id == id })

    private fun write(profiles: List<CalibrationProfile>): List<CalibrationProfile> {
        prefs.edit().putString(KEY, JSONArray(profiles.map { it.toJson() }).toString()).apply()
        return profiles
    }

    private companion object {
        const val KEY = "profiles"
    }
}
