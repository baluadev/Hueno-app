package com.hueno.coloridentifier.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Hueno keeps its interface neutral so the measured color is the only strong color on screen.
 *
 * Colors are named by role. Every screen reads them through [HuenoColors], which follows the
 * light or dark palette of the nearest [HuenoTheme]. The app follows the system setting; the camera
 * screen is always dark, so the preview is not framed by a bright glare.
 */
@Immutable
data class HuenoPalette(
    /** Behind everything. */
    val background: Color,
    /** Cards, rows, round buttons and other raised surfaces. */
    val surface: Color,
    /** Borders and dividers. */
    val line: Color,
    val text: Color,
    /** Secondary text. Still meets 4.5:1 on [background] and [surface]. */
    val textMuted: Color,
    /** Fill of the main action button. */
    val action: Color,
    val onAction: Color,
    /** Text links and text buttons; lighter than [action] on dark backgrounds to stay readable. */
    val link: Color,
    val isDark: Boolean,
)

private val LightPalette = HuenoPalette(
    // Near neutral: a warm page tints the swatches next to it.
    background = Color(0xFFF3F3F1),
    surface = Color(0xFFFFFFFF),
    line = Color(0xFFDCDCD8),
    text = Color(0xFF14161C),
    textMuted = Color(0xFF575D6B),
    action = Color(0xFF2F5FE0),
    onAction = Color(0xFFFFFFFF),
    link = Color(0xFF2453C8),
    isDark = false,
)

private val DarkPalette = HuenoPalette(
    background = Color(0xFF12141A),
    surface = Color(0xFF1C1F27),
    line = Color(0xFF2E3240),
    text = Color(0xFFF4F4F2),
    textMuted = Color(0xFFA9AFBD),
    action = Color(0xFF2F5FE0),
    onAction = Color(0xFFFFFFFF),
    link = Color(0xFF8EAEFF),
    isDark = true,
)

private val LocalHuenoPalette = staticCompositionLocalOf { DarkPalette }

/** The current palette's colors. Read inside composition only. */
object HuenoColors {
    val Background: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.background
    val Surface: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.surface
    val Line: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.line
    val Text: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.text
    val TextMuted: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.textMuted
    val Action: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.action
    val OnAction: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.onAction
    val Link: Color @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.link
    val IsDark: Boolean @Composable @ReadOnlyComposable get() = LocalHuenoPalette.current.isDark
}

/**
 * Colors that do not change with the theme: the splash, which must match the system splash, and
 * marks drawn over camera pictures, which can be any brightness.
 */
object HuenoBrand {
    val Ink = Color(0xFF12141A)
    val OnInk = Color(0xFFF4F4F2)
    val OnInkMuted = Color(0xFFA9AFBD)
}

/** The type scale. Every text size in the app is one of these. */
object HuenoType {
    /** Screen titles and the name of the color being read. */
    val Display = 32.sp
    /** Panel titles and comparison verdicts. */
    val Headline = 26.sp
    /** App bar title and card titles. */
    val Title = 20.sp
    /** Button labels and main body text. */
    val Body = 16.sp
    /** Secondary lines and descriptions. */
    val BodySmall = 14.sp
    /** Chips, field labels and tab labels. */
    val Label = 13.sp
    /** Fine print. */
    val Caption = 12.sp

    val DisplayLine = 38.sp
    val BodyLine = 22.sp
    val BodySmallLine = 20.sp
}

/** Corner radii. Pills and round buttons use CircleShape. */
object HuenoShapes {
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(16.dp)
    val Large = RoundedCornerShape(24.dp)
}

/** Shared sizes, so screens line up with each other. */
object HuenoSize {
    /** Side margin of every screen. */
    val ScreenPadding = 20.dp
    /** Height of the main action button. */
    val PrimaryButton = 56.dp
    /** Smallest touch target. */
    val Touch = 48.dp
}

/**
 * [dark] defaults to the system setting. Pass true to force the dark palette, as the camera does.
 */
@Composable
fun HuenoTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    HuenoTheme(darkness = if (dark) 1f else 0f, content = content)
}

/**
 * A palette part way between light (0) and dark (1). Animating [darkness] lets the whole frame,
 * text and navigation bar included, fade from one palette to the other instead of flashing when
 * the user moves between the always-dark camera and a light tab.
 */
@Composable
fun HuenoTheme(darkness: Float, content: @Composable () -> Unit) {
    val t = darkness.coerceIn(0f, 1f)
    val palette = when (t) {
        0f -> LightPalette
        1f -> DarkPalette
        else -> HuenoPalette(
            background = lerp(LightPalette.background, DarkPalette.background, t),
            surface = lerp(LightPalette.surface, DarkPalette.surface, t),
            line = lerp(LightPalette.line, DarkPalette.line, t),
            text = lerp(LightPalette.text, DarkPalette.text, t),
            textMuted = lerp(LightPalette.textMuted, DarkPalette.textMuted, t),
            action = lerp(LightPalette.action, DarkPalette.action, t),
            onAction = lerp(LightPalette.onAction, DarkPalette.onAction, t),
            link = lerp(LightPalette.link, DarkPalette.link, t),
            isDark = t >= 0.5f,
        )
    }
    val base = if (palette.isDark) darkColorScheme() else lightColorScheme()
    val scheme = base.copy(
        primary = palette.action,
        onPrimary = palette.onAction,
        background = palette.background,
        onBackground = palette.text,
        surface = palette.background,
        onSurface = palette.text,
        surfaceVariant = palette.surface,
        onSurfaceVariant = palette.textMuted,
        outline = palette.line,
        surfaceContainerHigh = palette.surface,
    )
    CompositionLocalProvider(LocalHuenoPalette provides palette) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
