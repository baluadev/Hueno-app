package com.hueno.coloridentifier.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/** Small pictures of the object a color was taken from. */
object Thumbnails {

    /** Side of a saved picture in pixels. */
    private const val SIDE = 192

    /** The crop covers this share of the picture's shorter side. */
    private const val CROP_FRACTION = 0.45f

    /**
     * A square crop of [source] centered on the spot at fraction ([fx], [fy]), shifted as needed
     * to stay inside the picture, and scaled down. Returns null if the picture is unusable.
     */
    fun around(source: Bitmap, fx: Float, fy: Float): Bitmap? = try {
        val side = max(1, (min(source.width, source.height) * CROP_FRACTION).toInt())
        val left = (fx * source.width - side / 2f).toInt().coerceIn(0, source.width - side)
        val top = (fy * source.height - side / 2f).toInt().coerceIn(0, source.height - side)
        val crop = Bitmap.createBitmap(source, left, top, side, side)
        Bitmap.createScaledBitmap(crop, SIDE, SIDE, true)
    } catch (e: Exception) {
        null
    }
}

/** Shows a saved picture, loaded off the main thread. Draws nothing until it is ready. */
@Composable
fun ThumbnailImage(file: File, contentDescription: String?, modifier: Modifier = Modifier) {
    val bitmap by produceState<Bitmap?>(initialValue = null, file) {
        value = withContext(Dispatchers.IO) { BitmapFactory.decodeFile(file.path) }
    }
    bitmap?.let {
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}
