package com.hueno.coloridentifier.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Line icons drawn on a 24 x 24 grid, matching the design canvas. Tinted by the caller. */
object HuenoIcons {

    private fun stroke(name: String, vararg paths: String, width: Float = 2f): ImageVector =
        ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f).apply {
            paths.forEach { data ->
                addPath(
                    pathData = addPathNodes(data),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = width,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    private const val CIRCLE_8 = "M4 12a8 8 0 1 0 16 0a8 8 0 1 0 -16 0"

    val Camera = stroke("Camera", "M4 8h3l2-3h6l2 3h3v11H4z", "M8.5 13a3.5 3.5 0 1 0 7 0a3.5 3.5 0 1 0 -7 0")
    val Photo = stroke(
        "Photo",
        "M6 5h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z",
        "M4 16l5-5 4 4 2-2 5 5",
    )
    val History = stroke("History", CIRCLE_8, "M12 8v4l3 2")
    val Speak = stroke("Speak", "M4 10v4h4l5 4V6l-5 4z", "M16.5 9a4 4 0 0 1 0 6", "M19 6.5a8 8 0 0 1 0 11")
    val Copy = stroke(
        "Copy",
        "M10 8h7a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2h-7a2 2 0 0 1-2-2v-8a2 2 0 0 1 2-2z",
        "M5 16V6a2 2 0 0 1 2-2h8",
    )
    val Torch = stroke("Torch", "M13 3 6 13h5l-1 8 7-10h-5z")
    val Calibrate = stroke("Calibrate", CIRCLE_8, "M12 4v16", "M12 8h7M12 12h8M12 16h7")
    val Pause = stroke("Pause", "M7 5h3v14H7z", "M14 5h3v14h-3z")
    val Play = stroke("Play", "M8 5v14l11-7z")
    val Back = stroke("Back", "M15 5l-7 7 7 7", width = 2.2f)
    val Close = stroke("Close", "M6 6l12 12M18 6 6 18", width = 2.2f)
    val Check = stroke("Check", "M5 12l5 5 9-10", width = 2.6f)
    val Compare = stroke(
        "Compare",
        "M3 12a6 6 0 1 0 12 0a6 6 0 1 0 -12 0",
        "M9 12a6 6 0 1 0 12 0a6 6 0 1 0 -12 0",
    )
    val Approx = stroke("Approx", "M5 9c2-2 4-2 7 0s5 2 7 0", "M5 15c2-2 4-2 7 0s5 2 7 0", width = 2.4f)
    val NotEqual = stroke("NotEqual", "M5 9h14M5 15h14M16 5 8 19", width = 2.4f)
    val Trash = stroke("Trash", "M5 7h14", "M10 7V5h4v2", "M7 7l1 12h8l1-12")
    val Settings = stroke(
        "Settings",
        "M4 7h10M18 7h2M4 17h2M10 17h10",
        "M14 7a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
        "M6 17a2 2 0 1 0 4 0a2 2 0 1 0 -4 0",
    )
    val Alert = stroke("Alert", CIRCLE_8, "M12 8v5", "M12 16v.5", width = 2.2f)
}
