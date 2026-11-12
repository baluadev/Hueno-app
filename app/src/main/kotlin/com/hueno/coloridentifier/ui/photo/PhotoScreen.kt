package com.hueno.coloridentifier.ui.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.Rgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

/** Longest side kept when decoding. Larger photos are scaled down to stay within memory. */
private const val MAX_SIDE = 4096

@Composable
fun PhotoScreen(onSave: (Rgb) -> Unit) {
    val context = LocalContext.current
    var uriText by rememberSaveable { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var failed by remember { mutableStateOf(false) }
    // Pointer position as a fraction of the photo, so it survives layout changes.
    var pointX by rememberSaveable { mutableStateOf(0.5f) }
    var pointY by rememberSaveable { mutableStateOf(0.5f) }

    // The system photo picker needs no storage permission.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriText = uri.toString()
            pointX = 0.5f
            pointY = 0.5f
        }
    }
    val pick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }

    LaunchedEffect(uriText) {
        val text = uriText ?: return@LaunchedEffect
        failed = false
        bitmap = withContext(Dispatchers.IO) { decode(context, Uri.parse(text)) }
        failed = bitmap == null
    }

    val photo = bitmap
    if (photo == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        ) {
            Text(
                stringResource(R.string.photo_empty_title),
                color = HuenoColors.OnInk,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(stringResource(R.string.photo_empty_body), color = HuenoColors.OnInkMuted, fontSize = 16.sp)
            if (failed) Text(stringResource(R.string.photo_error), color = HuenoColors.OnInk, fontSize = 15.sp)
            Button(
                onClick = pick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuenoColors.OnInk,
                    contentColor = HuenoColors.Ink,
                ),
            ) { Text(stringResource(R.string.photo_choose), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
        }
        return
    }

    val px = (pointX * (photo.width - 1)).toInt().coerceIn(0, photo.width - 1)
    val py = (pointY * (photo.height - 1)).toInt().coerceIn(0, photo.height - 1)
    val rgb = Rgb.fromArgb(photo.getPixel(px, py))
    val name = ColorNamer.name(rgb)

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.photo_title),
                color = HuenoColors.OnInk,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = pick, modifier = Modifier.height(48.dp)) {
                Text(stringResource(R.string.photo_choose_another), color = HuenoColors.OnInk)
            }
        }

        var box by remember { mutableStateOf(IntSize.Zero) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(HuenoColors.InkRaised)
                .onSizeChanged { box = it }
                .pointerInput(photo) {
                    detectTapGestures { tap ->
                        fractionOf(tap, box, photo)?.let { (x, y) ->
                            pointX = x
                            pointY = y
                        }
                    }
                }
                .pointerInput(photo) {
                    detectDragGestures { change, _ ->
                        fractionOf(change.position, box, photo)?.let { (x, y) ->
                            pointX = x
                            pointY = y
                        }
                    }
                },
        ) {
            Image(
                bitmap = photo.asImageBitmap(),
                contentDescription = stringResource(R.string.photo_description),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
            Canvas(Modifier.fillMaxSize()) {
                val rect = fittedRect(IntSize(size.width.toInt(), size.height.toInt()), photo)
                val center = Offset(rect.left + pointX * rect.width, rect.top + pointY * rect.height)
                drawCircle(HuenoColors.Ink, 26f, center, style = Stroke(width = 5f))
                drawCircle(Color.White, 22f, center, style = Stroke(width = 5f))
                drawCircle(Color.White, 3f, center)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(rgb.r, rgb.g, rgb.b))
                    .border(2.dp, HuenoColors.OnInk, RoundedCornerShape(16.dp)),
            )
            Column(Modifier.weight(1f)) {
                Text(name.plain, color = HuenoColors.OnInk, fontSize = 30.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.detail_and_hex, name.detail, rgb.hex),
                    color = HuenoColors.OnInkMuted,
                    fontSize = 15.sp,
                )
            }
        }
        Text(
            stringResource(R.string.photo_hint),
            color = HuenoColors.OnInkMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Button(
            onClick = { onSave(rgb) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HuenoColors.OnInk, contentColor = HuenoColors.Ink),
        ) { Text(stringResource(R.string.photo_save), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
    }
}

private data class FittedRect(val left: Float, val top: Float, val width: Float, val height: Float)

/** Where a photo lands inside [box] when scaled to fit. */
private fun fittedRect(box: IntSize, photo: Bitmap): FittedRect {
    val scale = min(box.width.toFloat() / photo.width, box.height.toFloat() / photo.height)
    val w = photo.width * scale
    val h = photo.height * scale
    return FittedRect((box.width - w) / 2, (box.height - h) / 2, w, h)
}

/** Converts a touch position to a fraction of the photo, or null when the box has no size yet. */
private fun fractionOf(position: Offset, box: IntSize, photo: Bitmap): Pair<Float, Float>? {
    if (box.width == 0 || box.height == 0) return null
    val rect = fittedRect(box, photo)
    val x = ((position.x - rect.left) / rect.width).coerceIn(0f, 1f)
    val y = ((position.y - rect.top) / rect.height).coerceIn(0f, 1f)
    return x to y
}

/** Decodes a photo into a software bitmap whose pixels can be read. Returns null on failure. */
private fun decode(context: Context, uri: Uri): Bitmap? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // ImageDecoder applies the EXIF orientation for us.
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, info, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            val longest = maxOf(info.size.width, info.size.height)
            if (longest > MAX_SIDE) decoder.setTargetSampleSize((longest + MAX_SIDE - 1) / MAX_SIDE)
        }
    } else {
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    }
} catch (e: Exception) {
    null
}
