package com.hueno.coloridentifier.ui

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.hueno.coloridentifier.R
import com.hueno.core.color.ChromaChange
import com.hueno.core.color.ColorFamily
import com.hueno.core.color.ColorName
import com.hueno.core.color.Comparison
import com.hueno.core.color.ExplanationKind
import com.hueno.core.color.LightnessChange
import com.hueno.core.color.Shade

/*
 * Color names and comparison sentences in the app language. The core module names colors in
 * English; these build the same answer from string resources, so every language gets whole names
 * ("Hellblau", "Bleu clair") and whole sentences instead of English pieces glued together.
 */

/** The plain name, for example "Light blue", in the app language. */
@Composable
fun ColorName.localizedPlain(): String = localizedPlain(LocalContext.current.resources)

fun ColorName.localizedPlain(res: Resources): String =
    if (reddish) res.getString(R.string.color_reddish_brown) else res.getString(plainRes(family, shade))

/** The sentence about how B differs from A, in the app language. */
@Composable
fun Comparison.localizedExplanation(): String = localizedExplanation(LocalContext.current.resources)

fun Comparison.localizedExplanation(res: Resources): String {
    val a = res.getString(inSentenceRes(familyA))
    val b = res.getString(inSentenceRes(familyB))
    val main = when (kind) {
        ExplanationKind.SAME -> return res.getString(R.string.cmp_same)
        ExplanationKind.CLOSE_ACROSS_BORDER -> res.getString(R.string.cmp_close_border, a, b)
        ExplanationKind.DIFFERENT_FAMILIES -> res.getString(R.string.cmp_different, a, b)
        ExplanationKind.SAME_FAMILY_TINT -> return res.getString(R.string.cmp_same_family_tint, b)
        ExplanationKind.SAME_FAMILY_CHANGED -> res.getString(R.string.cmp_same_family, b)
    }
    val change = changeRes(lightness, chroma) ?: return main
    return main + " " + res.getString(change)
}

@StringRes
private fun changeRes(l: LightnessChange, c: ChromaChange): Int? = when (l) {
    LightnessChange.NONE -> when (c) {
        ChromaChange.NONE -> null
        ChromaChange.MORE_VIVID -> R.string.cmp_change_vivid
        ChromaChange.DULLER -> R.string.cmp_change_duller
    }
    LightnessChange.LITTLE_LIGHTER -> when (c) {
        ChromaChange.NONE -> R.string.cmp_change_little_lighter
        ChromaChange.MORE_VIVID -> R.string.cmp_change_little_lighter_vivid
        ChromaChange.DULLER -> R.string.cmp_change_little_lighter_duller
    }
    LightnessChange.MUCH_LIGHTER -> when (c) {
        ChromaChange.NONE -> R.string.cmp_change_much_lighter
        ChromaChange.MORE_VIVID -> R.string.cmp_change_much_lighter_vivid
        ChromaChange.DULLER -> R.string.cmp_change_much_lighter_duller
    }
    LightnessChange.LITTLE_DARKER -> when (c) {
        ChromaChange.NONE -> R.string.cmp_change_little_darker
        ChromaChange.MORE_VIVID -> R.string.cmp_change_little_darker_vivid
        ChromaChange.DULLER -> R.string.cmp_change_little_darker_duller
    }
    LightnessChange.MUCH_DARKER -> when (c) {
        ChromaChange.NONE -> R.string.cmp_change_much_darker
        ChromaChange.MORE_VIVID -> R.string.cmp_change_much_darker_vivid
        ChromaChange.DULLER -> R.string.cmp_change_much_darker_duller
    }
}

@StringRes
private fun inSentenceRes(f: ColorFamily): Int = when (f) {
    ColorFamily.BLACK -> R.string.family_black
    ColorFamily.WHITE -> R.string.family_white
    ColorFamily.GRAY -> R.string.family_gray
    ColorFamily.RED -> R.string.family_red
    ColorFamily.ORANGE -> R.string.family_orange
    ColorFamily.YELLOW -> R.string.family_yellow
    ColorFamily.GREEN -> R.string.family_green
    ColorFamily.BLUE_GREEN -> R.string.family_blue_green
    ColorFamily.BLUE -> R.string.family_blue
    ColorFamily.PURPLE -> R.string.family_purple
    ColorFamily.PINK -> R.string.family_pink
    ColorFamily.BROWN -> R.string.family_brown
}

@StringRes
private fun plainRes(f: ColorFamily, s: Shade): Int = when (f) {
    ColorFamily.BLACK -> pick(s, R.string.color_black_light, R.string.color_black, R.string.color_black_dark)
    ColorFamily.WHITE -> pick(s, R.string.color_white_light, R.string.color_white, R.string.color_white_dark)
    ColorFamily.GRAY -> pick(s, R.string.color_gray_light, R.string.color_gray, R.string.color_gray_dark)
    ColorFamily.RED -> pick(s, R.string.color_red_light, R.string.color_red, R.string.color_red_dark)
    ColorFamily.ORANGE -> pick(s, R.string.color_orange_light, R.string.color_orange, R.string.color_orange_dark)
    ColorFamily.YELLOW -> pick(s, R.string.color_yellow_light, R.string.color_yellow, R.string.color_yellow_dark)
    ColorFamily.GREEN -> pick(s, R.string.color_green_light, R.string.color_green, R.string.color_green_dark)
    ColorFamily.BLUE_GREEN -> pick(s, R.string.color_blue_green_light, R.string.color_blue_green, R.string.color_blue_green_dark)
    ColorFamily.BLUE -> pick(s, R.string.color_blue_light, R.string.color_blue, R.string.color_blue_dark)
    ColorFamily.PURPLE -> pick(s, R.string.color_purple_light, R.string.color_purple, R.string.color_purple_dark)
    ColorFamily.PINK -> pick(s, R.string.color_pink_light, R.string.color_pink, R.string.color_pink_dark)
    ColorFamily.BROWN -> pick(s, R.string.color_brown_light, R.string.color_brown, R.string.color_brown_dark)
}

private fun pick(s: Shade, light: Int, normal: Int, dark: Int) = when (s) {
    Shade.LIGHT -> light
    Shade.NORMAL -> normal
    Shade.DARK -> dark
}
