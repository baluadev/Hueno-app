package com.hueno.data

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.util.UUID

/**
 * Small pictures of captured objects, kept as JPEG files in the app's private storage.
 * Files are addressed by name only, so a stored name can never point outside the folder.
 */
internal class ThumbnailStore(context: Context) {

    private val folder = File(context.applicationContext.filesDir, "thumbnails")

    /** Writes [bitmap] and returns the file name, or null when it could not be written. */
    fun save(bitmap: Bitmap): String? = try {
        folder.mkdirs()
        val name = "${UUID.randomUUID()}.jpg"
        File(folder, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, it) }
        name
    } catch (e: Exception) {
        null
    }

    /** The file for [name] if it is a plain file name and the file exists; otherwise null. */
    fun existing(name: String?): File? {
        if (name == null || !SAFE_NAME.matches(name)) return null
        return File(folder, name).takeIf { it.isFile }
    }

    fun delete(name: String?) {
        existing(name)?.delete()
    }

    private companion object {
        const val QUALITY = 85
        val SAFE_NAME = Regex("[A-Za-z0-9-]+\\.jpg")
    }
}
