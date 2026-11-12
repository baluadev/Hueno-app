package com.hueno.core.color

/** The plain color families Hueno speaks in. */
enum class ColorFamily(val label: String) {
    BLACK("Black"),
    WHITE("White"),
    GRAY("Gray"),
    RED("Red"),
    ORANGE("Orange"),
    YELLOW("Yellow"),
    GREEN("Green"),
    BLUE_GREEN("Blue-green"),
    BLUE("Blue"),
    PURPLE("Purple"),
    PINK("Pink"),
    BROWN("Brown"),
}

enum class Shade { LIGHT, NORMAL, DARK }

/**
 * Two-tier name for a color.
 * [plain] is what the app shows large ("Dark blue"); [detail] is the closest named shade ("Navy").
 */
data class ColorName(
    val family: ColorFamily,
    val shade: Shade,
    val plain: String,
    val detail: String,
)

/**
 * Turns a color into a plain family name plus the nearest detailed name.
 *
 * Family boundaries are first-draft thresholds in OKLCH. They are meant to be tuned
 * against printed swatches and real users; the tests pin today's behavior.
 */
object ColorNamer {

    fun name(rgb: Rgb): ColorName {
        val lch = ColorMath.toOklch(rgb)
        val family = familyOf(lch)
        val shade = shadeOf(family, lch)
        return ColorName(family, shade, plainLabel(family, shade, lch), NamedColors.nearest(rgb, family).name)
    }

    internal fun familyOf(c: Oklch): ColorFamily {
        // Near-neutral colors are decided first, so dull colors do not all collapse into "gray"
        // or pick up a hue name from sensor noise.
        if (c.l < 0.16) return ColorFamily.BLACK
        if (c.c < 0.035) {
            return when {
                c.l < 0.24 -> ColorFamily.BLACK
                c.l > 0.94 -> ColorFamily.WHITE
                else -> ColorFamily.GRAY
            }
        }
        val h = c.h
        return when {
            h >= 350 || h < 12 -> if (c.l >= 0.62) ColorFamily.PINK else ColorFamily.RED
            h < 38 -> when {
                c.l >= 0.74 -> ColorFamily.PINK
                c.l < 0.50 && c.c < 0.13 -> ColorFamily.BROWN
                else -> ColorFamily.RED
            }
            h < 75 -> if (c.l < 0.60 || (c.l < 0.72 && c.c < 0.09)) ColorFamily.BROWN else ColorFamily.ORANGE
            h < 112 -> if (c.l < 0.52) ColorFamily.BROWN else ColorFamily.YELLOW
            h < 170 -> ColorFamily.GREEN
            h < 215 -> ColorFamily.BLUE_GREEN
            h < 285 -> ColorFamily.BLUE
            h < 322 -> ColorFamily.PURPLE
            else -> if (c.l >= 0.62) ColorFamily.PINK else ColorFamily.PURPLE
        }
    }

    internal fun shadeOf(family: ColorFamily, c: Oklch): Shade = when (family) {
        ColorFamily.BLACK, ColorFamily.WHITE -> Shade.NORMAL
        ColorFamily.GRAY -> when {
            c.l >= 0.78 -> Shade.LIGHT
            c.l < 0.48 -> Shade.DARK
            else -> Shade.NORMAL
        }
        // Yellow is naturally very light, so its bands sit higher. Pure yellow is as light as
        // pale yellow, so "light" also requires the color to be washed out.
        ColorFamily.YELLOW -> when {
            c.l >= 0.93 && c.c < 0.14 -> Shade.LIGHT
            c.l < 0.80 -> Shade.DARK
            else -> Shade.NORMAL
        }
        // Saturated greens are darker than they look, so "dark" starts a little higher.
        ColorFamily.GREEN -> when {
            c.l >= 0.80 -> Shade.LIGHT
            c.l < 0.47 -> Shade.DARK
            else -> Shade.NORMAL
        }
        ColorFamily.PINK -> when {
            c.l >= 0.86 -> Shade.LIGHT
            c.l < 0.68 -> Shade.DARK
            else -> Shade.NORMAL
        }
        ColorFamily.BROWN -> when {
            c.l >= 0.62 -> Shade.LIGHT
            c.l < 0.42 -> Shade.DARK
            else -> Shade.NORMAL
        }
        else -> when {
            c.l >= 0.80 -> Shade.LIGHT
            c.l < 0.40 -> Shade.DARK
            else -> Shade.NORMAL
        }
    }

    private fun plainLabel(family: ColorFamily, shade: Shade, c: Oklch): String {
        // A brown that leans red is easier to act on as "Reddish brown" than as a shade of brown.
        if (family == ColorFamily.BROWN && shade == Shade.NORMAL && c.h < 52 && c.c >= 0.08) return "Reddish brown"
        return when (shade) {
            Shade.LIGHT -> "Light ${family.label.lowercase()}"
            Shade.DARK -> "Dark ${family.label.lowercase()}"
            Shade.NORMAL -> family.label
        }
    }
}
