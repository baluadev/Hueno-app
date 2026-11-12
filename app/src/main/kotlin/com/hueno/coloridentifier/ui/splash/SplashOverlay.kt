package com.hueno.coloridentifier.ui.splash

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private val Coral = Color(0xFFF2603D)
private val Blue = Color(0xFF3D6BF2)
private val Teal = Color(0xFF1DB8A6)
private val Amber = Color(0xFFF5B301)

/** The system splash draws its icon in a box of this size; the intro starts from the same picture. */
private val MARK_BOX = 288.dp

private const val INTRO_MS = 1300
private const val EXIT_MS = 280

/** The moment the ring snaps shut and the mark "locks on", as a share of the intro. */
private const val LOCK = 0.55f

/**
 * When the app underneath is started, as a share of the intro. By then the mark is whole again
 * (ring closed, tail back, center white, pulse gone), so if starting the app makes the intro
 * pause for a moment, it pauses on a finished picture.
 */
private const val REVEAL = 0.70f

/** Must equal the group scale in ic_splash.xml, so frame zero matches the system splash. */
private const val MARK_SCALE = 0.70f

/**
 * A short animated intro drawn over the app while it starts.
 *
 * The story is the product's: the aiming ring spins and opens, the center cycles through colors
 * as if searching, then the ring locks, a pulse goes out, and the name appears.
 *
 * [onReveal] fires just after the ring locks. The app is started then, not earlier: starting
 * the camera at the same time as the spin makes the spin stutter. The app then loads behind
 * the name and the fade, so the wait it adds is under a second.
 *
 * With system animations turned off, the still mark is shown briefly and the app follows.
 *
 * @param onReveal called once, when the app underneath should start.
 * @param onDone called once, when the intro has finished and can be removed.
 */
@Composable
fun SplashOverlay(onReveal: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    val intro = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        val animationsOff = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
        if (animationsOff) {
            intro.snapTo(1f)
            onReveal()
            delay(350)
        } else {
            intro.animateTo(REVEAL, tween((INTRO_MS * REVEAL).toInt(), easing = LinearEasing))
            onReveal()
            intro.animateTo(1f, tween((INTRO_MS * (1f - REVEAL)).toInt(), easing = LinearEasing))
            exit.animateTo(1f, tween(EXIT_MS))
        }
        onDone()
    }

    val t = intro.value
    val leaving = exit.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = 1f - leaving
                scaleX = 1f + 0.06f * leaving
                scaleY = 1f + 0.06f * leaving
            }
            .background(HuenoColors.Ink)
            // Swallows touches so nothing underneath is pressed by accident, and stays out of
            // the way of screen readers: the intro is decoration.
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent() } }
            .clearAndSetSemantics {},
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(MARK_BOX)) { drawMark(t) }

        val nameIn = smooth(span(t, REVEAL + 0.02f, 0.90f))
        val taglineIn = smooth(span(t, 0.80f, 0.98f))
        Column(
            modifier = Modifier.padding(top = 210.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                stringResource(R.string.app_name),
                color = HuenoColors.OnInk,
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.offset(y = (14 * (1f - nameIn)).dp).alpha(nameIn),
            )
            Text(
                stringResource(R.string.splash_tagline),
                color = HuenoColors.OnInkMuted,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 6.dp).alpha(taglineIn),
            )
        }
    }
}

/** Progress of [t] through the window [from]..[to], clamped to 0..1. */
private fun span(t: Float, from: Float, to: Float): Float = ((t - from) / (to - from)).coerceIn(0f, 1f)

/** Smoothstep: eases in and out. */
private fun smooth(x: Float): Float = x * x * (3f - 2f * x)

/**
 * Draws the mark at intro progress [t]. The geometry mirrors ic_splash.xml: a 512-unit viewport
 * scaled by [MARK_SCALE] around its center, so frame zero matches the system splash.
 */
private fun DrawScope.drawMark(t: Float) {
    val unit = size.minDimension / 512f
    val scale = MARK_SCALE
    fun point(x: Float, y: Float) = Offset((256f + (x - 256f) * scale) * unit, (256f + (y - 256f) * scale) * unit)

    val center = point(256f, 236f)
    val spin = span(t, 0f, LOCK)
    val wave = sin(PI.toFloat() * spin)

    // The ring turns once, swelling and opening gaps between its arcs, then snaps shut.
    val rotation = 360f * smooth(spin)
    val gap = 30f * wave
    val swell = 1f + 0.14f * wave
    val radius = 132f * scale * unit * swell
    val stroke = 52f * scale * unit
    val arcSize = Size(radius * 2, radius * 2)
    val arcTopLeft = Offset(center.x - radius, center.y - radius)
    listOf(-90f to Blue, 0f to Teal, 90f to Amber, 180f to Coral).forEach { (start, color) ->
        drawArc(
            color = color,
            startAngle = start + rotation + gap / 2,
            sweepAngle = 90f - gap,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = stroke, cap = if (gap > 1f) StrokeCap.Round else StrokeCap.Butt),
        )
    }

    // The speech-bubble tail leaves while the ring spins and pops back when it locks.
    val tail = (1f - smooth(span(t, 0f, 0.10f))) + smooth(span(t, LOCK - 0.04f, LOCK + 0.08f))
    if (tail > 0.01f) {
        val anchor = point(160f, 348f)
        fun grown(x: Float, y: Float): Offset {
            val p = point(x, y)
            return Offset(anchor.x + (p.x - anchor.x) * tail, anchor.y + (p.y - anchor.y) * tail)
        }
        val path = Path().apply {
            val a = grown(132f, 318f)
            val b = grown(104f, 424f)
            val c = grown(206f, 378f)
            moveTo(a.x, a.y)
            lineTo(b.x, b.y)
            lineTo(c.x, c.y)
            close()
        }
        drawPath(path, Amber.copy(alpha = tail.coerceIn(0f, 1f)))
    }

    // The center searches through the four hues, then settles on white as the ring locks.
    val search = span(t, 0.06f, LOCK - 0.04f)
    val hues = listOf(Coral, Amber, Teal, Blue, Coral)
    val position = search * (hues.size - 1)
    val index = position.toInt().coerceAtMost(hues.size - 2)
    val searching = lerp(hues[index], hues[index + 1], position - index)
    val settle = smooth(span(t, LOCK - 0.04f, LOCK + 0.06f))
    val showSearch = smooth(span(t, 0.02f, 0.10f))
    val dotColor = lerp(lerp(HuenoColors.OnInk, searching, showSearch), HuenoColors.OnInk, settle)
    val pop = sin(PI.toFloat() * span(t, LOCK - 0.02f, LOCK + 0.14f))
    drawCircle(dotColor, 44f * scale * unit * (1f + 0.28f * pop), center)

    // A pulse leaves the ring at the moment of lock.
    val pulse = span(t, LOCK, REVEAL)
    if (pulse > 0f && pulse < 1f) {
        val outer = 158f * scale * unit
        drawCircle(
            color = HuenoColors.OnInk.copy(alpha = 0.55f * (1f - pulse)),
            radius = outer * (1f + 0.75f * smooth(pulse)),
            center = center,
            style = Stroke(width = 6f * unit * (1f - 0.6f * pulse)),
        )
    }
}
