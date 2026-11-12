package com.hueno.coloridentifier.ui.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoBrand
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.coloridentifier.ui.HuenoPrimaryButton
import com.hueno.coloridentifier.ui.HuenoSecondaryButton
import com.hueno.coloridentifier.ui.HuenoSize
import com.hueno.coloridentifier.ui.TabTitle
import com.hueno.coloridentifier.ui.HuenoShapes
import com.hueno.coloridentifier.ui.HuenoType
import com.hueno.coloridentifier.ui.Thumbnails
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.Rgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min

/** Longest side kept when decoding. Larger photos are scaled down to stay within memory. */
private const val MAX_SIDE = 4096

private const val MIN_ZOOM = 1f
private const val MAX_ZOOM = 12f

/** Above this zoom, pixels are drawn as crisp squares so single pixels can be picked. */
private const val PIXEL_ZOOM = 4f

@Composable
fun PhotoScreen(onSave: (Rgb, Bitmap?) -> Unit) {
    val context = LocalContext.current
    var uriText by rememberSaveable { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var failed by remember { mutableStateOf(false) }
    // Pointer position as a fraction of the photo, so it survives layout changes.
    var pointX by rememberSaveable { mutableFloatStateOf(0.5f) }
    var pointY by rememberSaveable { mutableFloatStateOf(0.5f) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }

    // The system photo picker needs no storage permission.
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uriText = uri.toString()
            pointX = 0.5f
            pointY = 0.5f
            zoom = 1f
            pan = Offset.Zero
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
        // Same bar as the loaded state and the other tabs, with the content starting under it.
        Column(Modifier.fillMaxSize()) {
            PhotoBar()
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = HuenoSize.ScreenPadding, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.photo_empty_title),
                    color = HuenoColors.Text,
                    fontSize = HuenoType.Headline,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.photo_empty_body),
                    color = HuenoColors.TextMuted,
                    fontSize = HuenoType.Body,
                    lineHeight = HuenoType.BodyLine,
                )
                if (failed) Text(stringResource(R.string.photo_error), color = HuenoColors.Text, fontSize = HuenoType.Body)
                HuenoPrimaryButton(
                    text = stringResource(R.string.photo_choose),
                    onClick = pick,
                    icon = HuenoIcons.Photo,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
        return
    }

    val px = (pointX * (photo.width - 1)).toInt().coerceIn(0, photo.width - 1)
    val py = (pointY * (photo.height - 1)).toInt().coerceIn(0, photo.height - 1)
    val rgb = Rgb.fromArgb(photo.getPixel(px, py))
    val name = ColorNamer.name(rgb)

    Column(Modifier.fillMaxSize()) {
        PhotoBar {
            HuenoSecondaryButton(stringResource(R.string.photo_choose_another), pick)
        }

        var box by remember { mutableStateOf(IntSize.Zero) }

        /** Applies a zoom step around [focus] (a point in the box) and keeps the photo in view. */
        fun zoomBy(factor: Float, focus: Offset, drag: Offset = Offset.Zero) {
            val view = View(box, photo, zoom, pan)
            val newZoom = (zoom * factor).coerceIn(MIN_ZOOM, MAX_ZOOM)
            val applied = newZoom / zoom
            // Keep the photo point under the focus where it is while scaling, then apply the drag.
            val fromCenter = focus - view.center
            val moved = (pan - fromCenter) * applied + fromCenter + drag
            zoom = newZoom
            pan = View(box, photo, newZoom, moved).clampedPan()
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(HuenoShapes.Large)
                .background(HuenoColors.Surface),
        ) {
            // The photo and its gestures live in their own layer. The zoom buttons are a sibling of
            // it, not a child, so pressing a button never also moves the ring.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { box = it }
                    .pointerInput(photo) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            // One finger moves the ring; a second finger switches the gesture to zooming
                            // and panning for the rest of it, so lifting a finger does not jump the ring.
                            var zooming = false
                            View(box, photo, zoom, pan).fractionAt(down.position)?.let { (x, y) ->
                                pointX = x
                                pointY = y
                            }
                            do {
                                val event = awaitPointerEvent()
                                val pressed = event.changes.filter { it.pressed }
                                if (pressed.size >= 2) {
                                    zooming = true
                                    zoomBy(event.calculateZoom(), event.calculateCentroid(useCurrent = false), event.calculatePan())
                                } else if (pressed.size == 1 && !zooming) {
                                    View(box, photo, zoom, pan).fractionAt(pressed[0].position)?.let { (x, y) ->
                                        pointX = x
                                        pointY = y
                                    }
                                }
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            ) {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = stringResource(R.string.photo_description),
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                        translationX = pan.x
                        translationY = pan.y
                    },
                    contentScale = ContentScale.Fit,
                    filterQuality = if (zoom >= PIXEL_ZOOM) FilterQuality.None else FilterQuality.Low,
                )
                Canvas(Modifier.fillMaxSize()) {
                    val view = View(IntSize(size.width.toInt(), size.height.toInt()), photo, zoom, pan)
                    val center = view.positionOf(pointX, pointY)
                    drawCircle(HuenoBrand.Ink, 26f, center, style = Stroke(width = 5f))
                    drawCircle(Color.White, 22f, center, style = Stroke(width = 5f))
                    drawCircle(Color.White, 3f, center)
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .clip(HuenoShapes.Large)
                    .background(HuenoColors.Background)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // The buttons zoom around the ring, so the picked spot stays in view. The ring's
                // position is read at click time, from the current zoom and pan.
                ZoomButton("−", stringResource(R.string.photo_zoom_out), zoom > MIN_ZOOM) {
                    zoomBy(0.5f, View(box, photo, zoom, pan).positionOf(pointX, pointY))
                }
                ZoomButton("+", stringResource(R.string.photo_zoom_in), zoom < MAX_ZOOM) {
                    zoomBy(2f, View(box, photo, zoom, pan).positionOf(pointX, pointY))
                }
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
                    .clip(HuenoShapes.Medium)
                    .background(Color(rgb.r, rgb.g, rgb.b))
                    .border(2.dp, HuenoColors.Text, HuenoShapes.Medium),
            )
            Column(Modifier.weight(1f)) {
                Text(name.plain, color = HuenoColors.Text, fontSize = HuenoType.Display, lineHeight = HuenoType.DisplayLine, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(R.string.detail_and_hex, name.detail, rgb.hex),
                    color = HuenoColors.TextMuted,
                    fontSize = HuenoType.Body,
                )
            }
        }
        Text(
            stringResource(R.string.photo_hint),
            color = HuenoColors.TextMuted,
            fontSize = HuenoType.Label,
            modifier = Modifier.padding(horizontal = HuenoSize.ScreenPadding),
        )
        HuenoPrimaryButton(
            text = stringResource(R.string.photo_save),
            onClick = { onSave(rgb, Thumbnails.around(photo, pointX, pointY)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HuenoSize.ScreenPadding, vertical = 12.dp),
        )
    }
}

/** The bar at the top of the Photo tab, laid out like the other tabs' bars. */
@Composable
private fun PhotoBar(actions: @Composable () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(start = HuenoSize.ScreenPadding, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TabTitle(stringResource(R.string.photo_title), Modifier.weight(1f), showLogo = true)
        actions()
    }
}

@Composable
private fun ZoomButton(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            symbol,
            color = if (enabled) HuenoColors.Text else HuenoColors.TextMuted,
            fontSize = HuenoType.Headline,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * The geometry of a photo shown in a box: fitted to the box, then scaled by [zoom] around the
 * box center and shifted by [pan]. Converts between touch positions and fractions of the photo.
 */
private class View(private val box: IntSize, photo: Bitmap, private val zoom: Float, private val pan: Offset) {

    val center = Offset(box.width / 2f, box.height / 2f)

    private val fit = if (box.width == 0 || box.height == 0) {
        0f
    } else {
        min(box.width.toFloat() / photo.width, box.height.toFloat() / photo.height)
    }
    private val fittedWidth = photo.width * fit
    private val fittedHeight = photo.height * fit
    private val left = (box.width - fittedWidth) / 2
    private val top = (box.height - fittedHeight) / 2

    /** Screen position of a point given as a fraction of the photo. */
    fun positionOf(fx: Float, fy: Float): Offset {
        val unzoomed = Offset(left + fx * fittedWidth, top + fy * fittedHeight)
        return center + (unzoomed - center) * zoom + pan
    }

    /** Fraction of the photo under a touch position, clamped to the photo; null before layout. */
    fun fractionAt(position: Offset): Pair<Float, Float>? {
        if (fit == 0f) return null
        val unzoomed = center + (position - pan - center) / zoom
        val x = ((unzoomed.x - left) / fittedWidth).coerceIn(0f, 1f)
        val y = ((unzoomed.y - top) / fittedHeight).coerceIn(0f, 1f)
        return x to y
    }

    /** The pan limited so the zoomed photo cannot be dragged away from the box. */
    fun clampedPan(): Offset {
        val maxX = max(0f, (fittedWidth * zoom - box.width) / 2)
        val maxY = max(0f, (fittedHeight * zoom - box.height) / 2)
        return Offset(pan.x.coerceIn(-maxX, maxX), pan.y.coerceIn(-maxY, maxY))
    }
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
        // Older Android does not apply the photo's stored orientation, so it is done by hand.
        val decoded = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        val degrees = context.contentResolver.openInputStream(uri)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } ?: 0f
        if (decoded != null && degrees != 0f) {
            Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(degrees) }, true)
        } else {
            decoded
        }
    }
} catch (e: Exception) {
    null
}
