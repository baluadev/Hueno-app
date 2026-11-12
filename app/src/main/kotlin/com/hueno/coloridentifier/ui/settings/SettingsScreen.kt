package com.hueno.coloridentifier.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
            .background(HuenoColors.Paper)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val back = stringResource(R.string.back)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onClose)
                    .semantics { contentDescription = back },
                contentAlignment = Alignment.Center,
            ) { Icon(HuenoIcons.Back, contentDescription = null, tint = HuenoColors.OnPaper) }
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                stringResource(R.string.settings_title),
                color = HuenoColors.OnPaper,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() },
            )

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
                        Text(label, color = HuenoColors.OnPaper, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                        Text(
                            stringResource(
                                if (hexWithHash) R.string.settings_hex_hash_on else R.string.settings_hex_hash_off,
                            ),
                            color = HuenoColors.OnPaperMuted,
                            fontSize = 14.sp,
                        )
                    }
                    Switch(checked = hexWithHash, onCheckedChange = null)
                }
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
                    HorizontalDivider(color = HuenoColors.PaperLine)
                    LinkRow(stringResource(R.string.pro_privacy), stringResource(R.string.settings_privacy_body), onPrivacy)
                }
                if (onFeedback != null) {
                    HorizontalDivider(color = HuenoColors.PaperLine)
                    LinkRow(
                        stringResource(R.string.settings_feedback),
                        stringResource(R.string.settings_feedback_body),
                        onFeedback,
                    )
                }
            }

            Text(
                stringResource(R.string.settings_version, versionName),
                color = HuenoColors.OnPaperMuted,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun Group(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HuenoColors.Card)
            .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(14.dp)),
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
            Text(title, color = HuenoColors.OnPaper, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Text(body, color = HuenoColors.OnPaperMuted, fontSize = 14.sp)
        }
    }
}
