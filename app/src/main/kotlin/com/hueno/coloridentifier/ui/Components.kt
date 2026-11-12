package com.hueno.coloridentifier.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hueno.coloridentifier.R

/** The one main action of a screen: filled, full height, in the action color. */
@Composable
fun HuenoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = HuenoSize.PrimaryButton),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = HuenoColors.Action,
            contentColor = HuenoColors.OnAction,
            disabledContainerColor = HuenoColors.Line,
            disabledContentColor = HuenoColors.TextMuted,
        ),
    ) { ButtonContent(text, icon) }
}

/** Any other action: outlined, in the text color, never competing with the primary button. */
@Composable
fun HuenoSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = HuenoSize.Touch),
        shape = CircleShape,
        border = BorderStroke(1.dp, HuenoColors.Line),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = HuenoColors.Text,
            disabledContentColor = HuenoColors.TextMuted,
        ),
    ) { ButtonContent(text, icon) }
}

@Composable
private fun ButtonContent(text: String, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
    }
    Text(text, fontSize = HuenoType.Body, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
}

/**
 * Top of every screen drawn over the tabs (color detail, settings, Pro): a back button, an
 * optional title, and an optional action at the end. All of them go back the same way.
 */
@Composable
fun OverlayHeader(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val back = stringResource(R.string.back)
        Box(
            modifier = Modifier
                .size(HuenoSize.Touch)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = back },
            contentAlignment = Alignment.Center,
        ) { Icon(HuenoIcons.Back, contentDescription = null, tint = HuenoColors.Text) }
        if (title != null) {
            Text(
                title,
                color = HuenoColors.Text,
                fontSize = HuenoType.Title,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        trailing()
    }
}

/** The large title at the top of a tab or overlay. */
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        color = HuenoColors.Text,
        fontSize = HuenoType.Display,
        lineHeight = HuenoType.DisplayLine,
        fontWeight = FontWeight.Bold,
        modifier = modifier.semantics { heading() },
    )
}

/**
 * Title in the bar at the top of a tab. Every tab uses this size, so switching tabs does not
 * make the title jump.
 */
@Composable
fun TabTitle(text: String, modifier: Modifier = Modifier, showLogo: Boolean = false) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (showLogo) {
            // Decorative: the title next to it already says where the user is.
            Image(
                painter = painterResource(R.drawable.hueno_mark),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text,
            color = HuenoColors.Text,
            fontSize = HuenoType.Title,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() },
        )
    }
}

/**
 * "Just now", "5 min ago", "3 h ago", "Yesterday", or a date. Always in English, like the rest of
 * the app; the system's relative-time formatter follows the phone language instead.
 */
fun relativeTime(thenMillis: Long, nowMillis: Long): String {
    val minutes = (nowMillis - thenMillis) / 60_000
    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        minutes < 24 * 60 -> "${minutes / 60} h ago"
        minutes < 48 * 60 -> "Yesterday"
        else -> java.text.SimpleDateFormat("MMM d", java.util.Locale.US).format(java.util.Date(thenMillis))
    }
}
