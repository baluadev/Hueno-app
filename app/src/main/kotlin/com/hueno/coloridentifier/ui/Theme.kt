package com.hueno.coloridentifier.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Hueno keeps its interface neutral so the measured color is the only strong color on screen.
 * Camera screens use the dark palette, reading screens the light one.
 */
object HuenoColors {
    val Ink = Color(0xFF12141A)
    val InkRaised = Color(0xFF1C1F27)
    val InkLine = Color(0xFF2B2F3A)
    val OnInk = Color(0xFFF6F4EF)
    val OnInkMuted = Color(0xFFA9AFBD)

    val Paper = Color(0xFFF6F4EF)
    val Card = Color(0xFFFFFFFF)
    val PaperLine = Color(0xFFE2DED4)
    val OnPaper = Color(0xFF14161C)
    val OnPaperMuted = Color(0xFF5B6170)

    val Action = Color(0xFF2F5FE0)
    val OnAction = Color(0xFFFFFFFF)
}

private val LightScheme = lightColorScheme(
    primary = HuenoColors.Action,
    onPrimary = HuenoColors.OnAction,
    background = HuenoColors.Paper,
    onBackground = HuenoColors.OnPaper,
    surface = HuenoColors.Paper,
    onSurface = HuenoColors.OnPaper,
    surfaceVariant = HuenoColors.Card,
    onSurfaceVariant = HuenoColors.OnPaperMuted,
    outline = HuenoColors.PaperLine,
)

private val DarkScheme = darkColorScheme(
    primary = HuenoColors.OnInk,
    onPrimary = HuenoColors.Ink,
    background = HuenoColors.Ink,
    onBackground = HuenoColors.OnInk,
    surface = HuenoColors.Ink,
    onSurface = HuenoColors.OnInk,
    surfaceVariant = HuenoColors.InkRaised,
    onSurfaceVariant = HuenoColors.OnInkMuted,
    outline = HuenoColors.InkLine,
)

/** The palette is chosen per screen, not from the system setting: [dark] is true on camera screens. */
@Composable
fun HuenoTheme(dark: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme, content = content)
}
