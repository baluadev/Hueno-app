package com.hueno.core.color

data class NamedColor(val name: String, val rgb: Rgb)

/**
 * Detailed color names. This starter table uses CSS named colors, which are part of an open
 * web standard. It is data, not logic: extend or replace it without touching the namer.
 */
object NamedColors {

    private val raw = listOf(
        "Black" to "#000000", "Dim gray" to "#696969", "Gray" to "#808080", "Dark gray" to "#A9A9A9",
        "Silver" to "#C0C0C0", "Light gray" to "#D3D3D3", "Gainsboro" to "#DCDCDC", "White smoke" to "#F5F5F5",
        "White" to "#FFFFFF", "Snow" to "#FFFAFA", "Ivory" to "#FFFFF0", "Linen" to "#FAF0E6",
        "Beige" to "#F5F5DC", "Antique white" to "#FAEBD7", "Slate gray" to "#708090", "Dark slate gray" to "#2F4F4F",
        "Maroon" to "#800000", "Dark red" to "#8B0000", "Brown" to "#A52A2A", "Firebrick" to "#B22222",
        "Crimson" to "#DC143C", "Red" to "#FF0000", "Tomato" to "#FF6347", "Coral" to "#FF7F50",
        "Indian red" to "#CD5C5C", "Light coral" to "#F08080", "Salmon" to "#FA8072", "Light salmon" to "#FFA07A",
        "Orange red" to "#FF4500", "Dark orange" to "#FF8C00", "Orange" to "#FFA500", "Gold" to "#FFD700",
        "Saddle brown" to "#8B4513", "Sienna" to "#A0522D", "Chocolate" to "#D2691E", "Peru" to "#CD853F",
        "Sandy brown" to "#F4A460", "Burlywood" to "#DEB887", "Tan" to "#D2B48C", "Rosy brown" to "#BC8F8F",
        "Wheat" to "#F5DEB3", "Moccasin" to "#FFE4B5", "Peach puff" to "#FFDAB9", "Khaki" to "#F0E68C",
        "Dark khaki" to "#BDB76B", "Goldenrod" to "#DAA520", "Dark goldenrod" to "#B8860B", "Yellow" to "#FFFF00",
        "Light yellow" to "#FFFFE0", "Lemon chiffon" to "#FFFACD", "Olive" to "#808000", "Olive drab" to "#6B8E23",
        "Yellow green" to "#9ACD32", "Dark olive green" to "#556B2F", "Lawn green" to "#7CFC00", "Lime" to "#00FF00",
        "Lime green" to "#32CD32", "Pale green" to "#98FB98", "Light green" to "#90EE90", "Spring green" to "#00FF7F",
        "Medium sea green" to "#3CB371", "Sea green" to "#2E8B57", "Forest green" to "#228B22", "Green" to "#008000",
        "Dark green" to "#006400", "Dark sea green" to "#8FBC8F", "Medium aquamarine" to "#66CDAA", "Aquamarine" to "#7FFFD4",
        "Turquoise" to "#40E0D0", "Light sea green" to "#20B2AA", "Dark cyan" to "#008B8B", "Teal" to "#008080",
        "Cyan" to "#00FFFF", "Light cyan" to "#E0FFFF", "Pale turquoise" to "#AFEEEE", "Cadet blue" to "#5F9EA0",
        "Powder blue" to "#B0E0E6", "Light blue" to "#ADD8E6", "Sky blue" to "#87CEEB", "Deep sky blue" to "#00BFFF",
        "Steel blue" to "#4682B4", "Dodger blue" to "#1E90FF", "Cornflower blue" to "#6495ED", "Royal blue" to "#4169E1",
        "Blue" to "#0000FF", "Medium blue" to "#0000CD", "Dark blue" to "#00008B", "Navy" to "#000080",
        "Midnight blue" to "#191970", "Light steel blue" to "#B0C4DE", "Slate blue" to "#6A5ACD", "Dark slate blue" to "#483D8B",
        "Medium purple" to "#9370DB", "Blue violet" to "#8A2BE2", "Indigo" to "#4B0082", "Dark orchid" to "#9932CC",
        "Dark violet" to "#9400D3", "Purple" to "#800080", "Dark magenta" to "#8B008B", "Magenta" to "#FF00FF",
        "Orchid" to "#DA70D6", "Violet" to "#EE82EE", "Plum" to "#DDA0DD", "Thistle" to "#D8BFD8",
        "Lavender" to "#E6E6FA", "Medium violet red" to "#C71585", "Deep pink" to "#FF1493", "Hot pink" to "#FF69B4",
        "Pale violet red" to "#DB7093", "Light pink" to "#FFB6C1", "Pink" to "#FFC0CB", "Misty rose" to "#FFE4E1",
    )

    val all: List<NamedColor> = raw.map { (name, hex) -> NamedColor(name, Rgb.parse(hex)) }

    private val labs: List<Lab> = all.map { ColorMath.toLab(it.rgb) }

    private val families: List<ColorFamily> = all.map { ColorNamer.familyOf(ColorMath.toOklch(it.rgb)) }

    /**
     * The named color with the smallest CIEDE2000 difference from [rgb].
     *
     * With [family] given, names from that family win, so the detail never contradicts the plain
     * name (a color shown as "Brown" is not subtitled "Gray"). If the table has no name in that
     * family, the overall nearest is returned.
     */
    fun nearest(rgb: Rgb, family: ColorFamily? = null): NamedColor {
        val lab = ColorMath.toLab(rgb)
        var best = -1
        var bestDistance = Double.MAX_VALUE
        var bestInFamily = -1
        var bestInFamilyDistance = Double.MAX_VALUE
        for (i in all.indices) {
            val d = ColorDifference.ciede2000(lab, labs[i])
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
            if (family != null && families[i] == family && d < bestInFamilyDistance) {
                bestInFamilyDistance = d
                bestInFamily = i
            }
        }
        return all[if (bestInFamily >= 0) bestInFamily else best]
    }
}
