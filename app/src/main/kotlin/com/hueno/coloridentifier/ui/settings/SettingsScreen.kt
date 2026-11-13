package com.hueno.coloridentifier.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoSize
import com.hueno.coloridentifier.ui.OverlayHeader
import com.hueno.coloridentifier.ui.HuenoShapes
import com.hueno.coloridentifier.ui.HuenoType
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.monetization.MonetizationState

/**
 * @param onPrivacy shown only where the law requires a way to revisit the ad consent choice.
 * @param onFeedback null when no feedback address is configured, which hides the row.
 */
@Composable
fun SettingsScreen(
    money: MonetizationState,
    hexWithHash: Boolean,
    versionName: String,
    onHexWithHash: (Boolean) -> Unit,
    onOpenPro: () -> Unit,
    onPrivacy: () -> Unit,
    onFeedback: (() -> Unit)?,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuenoColors.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        OverlayHeader(onBack = onClose, title = stringResource(R.string.settings_title))

        Column(
            modifier = Modifier.padding(horizontal = HuenoSize.ScreenPadding).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            Group {
                val label = stringResource(R.string.settings_hex_hash)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .toggleable(value = hexWithHash, role = Role.Switch, onValueChange = onHexWithHash)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(label, color = HuenoColors.Text, fontSize = HuenoType.Body, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(
                                if (hexWithHash) R.string.settings_hex_hash_on else R.string.settings_hex_hash_off,
                            ),
                            color = HuenoColors.TextMuted,
                            fontSize = HuenoType.BodySmall,
                        )
                    }
                    Switch(checked = hexWithHash, onCheckedChange = null)
                }
            }

            Group {
                var choosingLanguage by remember { mutableStateOf(false) }
                LinkRow(stringResource(R.string.settings_language), currentLanguageName()) { choosingLanguage = true }
                if (choosingLanguage) LanguageDialog(onDismiss = { choosingLanguage = false })
            }

            Group {
                val proStatus = when {
                    money.purchased -> stringResource(R.string.pro_status_purchased)
                    money.passRemainingMillis > 0 -> {
                        val minutes = (money.passRemainingMillis + 59_999) / 60_000
                        stringResource(R.string.settings_pass_left, minutes / 60, minutes % 60)
                    }
                    else -> stringResource(R.string.settings_pro_free)
                }
                LinkRow(stringResource(R.string.pro_title), proStatus, onOpenPro)
                if (money.privacyOptionsRequired) {
                    HorizontalDivider(color = HuenoColors.Line)
                    LinkRow(stringResource(R.string.pro_privacy), stringResource(R.string.settings_privacy_body), onPrivacy)
                }
                if (onFeedback != null) {
                    HorizontalDivider(color = HuenoColors.Line)
                    LinkRow(
                        stringResource(R.string.settings_feedback),
                        stringResource(R.string.settings_feedback_body),
                        onFeedback,
                    )
                }
            }

            Text(
                stringResource(R.string.settings_version, versionName),
                color = HuenoColors.TextMuted,
                fontSize = HuenoType.Label,
            )
            // Required by the icons' CC BY 4.0 license: credit plus a link to Streamline.
            val uriHandler = LocalUriHandler.current
            Text(
                stringResource(R.string.settings_icon_credit),
                color = HuenoColors.Link,
                fontSize = HuenoType.Label,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button) { runCatching { uriHandler.openUri(ICON_CREDIT_URL) } }
                    .wrapContentHeight(),
            )
        }
    }
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(HuenoShapes.Medium)
            .background(HuenoColors.Surface)
            .border(1.dp, HuenoColors.Line, HuenoShapes.Medium),
    ) { content() }
}

@Composable
private fun LinkRow(title: String, body: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = HuenoColors.Text, fontSize = HuenoType.Body, fontWeight = FontWeight.Medium)
            Text(body, color = HuenoColors.TextMuted, fontSize = HuenoType.BodySmall)
        }
    }
}

private const val ICON_CREDIT_URL = "https://streamlinehq.com"
