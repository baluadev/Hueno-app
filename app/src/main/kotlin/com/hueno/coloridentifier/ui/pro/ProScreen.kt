package com.hueno.coloridentifier.ui.pro

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoSize
import com.hueno.coloridentifier.ui.OverlayHeader
import com.hueno.coloridentifier.ui.ScreenTitle
import com.hueno.coloridentifier.ui.HuenoShapes
import com.hueno.coloridentifier.ui.HuenoType
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.coloridentifier.ui.ProReason
import com.hueno.monetization.MonetizationState
import com.hueno.monetization.ProRules

@Composable
fun ProScreen(
    reason: ProReason,
    state: MonetizationState,
    rewardedBusy: Boolean,
    message: String?,
    onClose: () -> Unit,
    onBuy: () -> Unit,
    onWatch: () -> Unit,
    onRestore: () -> Unit,
    onPrivacy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuenoColors.Background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState()),
    ) {
        OverlayHeader(onBack = onClose) {
            TextButton(onClick = onRestore) {
                Text(stringResource(R.string.pro_restore), color = HuenoColors.Link, fontWeight = FontWeight.SemiBold)
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = HuenoSize.ScreenPadding).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ScreenTitle(stringResource(R.string.pro_title))
                val limit = when (reason) {
                    ProReason.COMPARE_LIMIT ->
                        stringResource(R.string.pro_reason_compare, ProRules.FREE_COMPARES_PER_DAY)
                    ProReason.HISTORY_LIMIT ->
                        stringResource(R.string.pro_reason_history, ProRules.FREE_HISTORY_LIMIT)
                    ProReason.EXPORT -> stringResource(R.string.pro_reason_export)
                    ProReason.PROFILE_LIMIT ->
                        stringResource(R.string.pro_reason_profile, ProRules.FREE_PROFILE_LIMIT)
                    ProReason.MENU -> null
                }
                limit?.let { Text(it, color = HuenoColors.Text, fontSize = HuenoType.Body, fontWeight = FontWeight.SemiBold) }
                Text(stringResource(R.string.pro_intro), color = HuenoColors.TextMuted, fontSize = HuenoType.Body, lineHeight = HuenoType.BodyLine)
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Benefit(stringResource(R.string.pro_benefit_ads))
                Benefit(stringResource(R.string.pro_benefit_history))
                Benefit(stringResource(R.string.pro_benefit_compare))
                Benefit(stringResource(R.string.pro_benefit_export))
                Benefit(stringResource(R.string.pro_benefit_profiles))
            }

            StatusNote(state)
            // A live region, so TalkBack reads the outcome of a purchase or restore without a search.
            message?.let {
                Text(
                    it,
                    color = HuenoColors.Text,
                    fontSize = HuenoType.Body,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }

            if (!state.purchased) {
                val price = state.price
                OptionCard(
                    title = stringResource(R.string.pro_buy_title),
                    subtitle = stringResource(
                        if (price != null) R.string.pro_buy_subtitle else R.string.pro_buy_unavailable,
                    ),
                    trailing = price,
                    icon = null,
                    filled = true,
                    enabled = price != null,
                    onClick = onBuy,
                )
                val watchSubtitle = when {
                    rewardedBusy -> stringResource(R.string.pro_watch_loading)
                    state.passAtCap -> stringResource(R.string.pro_watch_at_cap, ProRules.MAX_PASS_HOURS)
                    else -> stringResource(R.string.pro_watch_subtitle, ProRules.MAX_PASS_HOURS)
                }
                OptionCard(
                    title = pluralStringResource(R.plurals.pro_watch_title, ProRules.PASS_HOURS, ProRules.PASS_HOURS),
                    subtitle = watchSubtitle,
                    trailing = null,
                    icon = HuenoIcons.Play,
                    filled = false,
                    enabled = !rewardedBusy && !state.passAtCap,
                    onClick = onWatch,
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.pro_videos_note), color = HuenoColors.TextMuted, fontSize = HuenoType.Label)
                if (state.privacyOptionsRequired) {
                    TextButton(onClick = onPrivacy) {
                        Text(stringResource(R.string.pro_privacy), color = HuenoColors.Link, fontSize = HuenoType.BodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(HuenoIcons.Check, contentDescription = null, tint = HuenoColors.Text, modifier = Modifier.size(22.dp))
        Text(text, color = HuenoColors.Text, fontSize = HuenoType.Body)
    }
}

/** Where the user stands right now: bought, on a pass with time left, or on the free tier. */
@Composable
private fun StatusNote(state: MonetizationState) {
    val text = when {
        state.purchased -> stringResource(R.string.pro_status_purchased)
        state.passRemainingMillis > 0 -> {
            val minutes = (state.passRemainingMillis + 59_999) / 60_000
            stringResource(R.string.pro_status_pass, minutes / 60, minutes % 60)
        }
        else -> stringResource(R.string.pro_status_free)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HuenoShapes.Medium)
            .background(HuenoColors.Surface)
            .border(1.dp, HuenoColors.Line, HuenoShapes.Medium)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            if (state.isPro) HuenoIcons.Check else HuenoIcons.History,
            contentDescription = null,
            tint = HuenoColors.Text,
            modifier = Modifier.size(20.dp),
        )
        Text(text, color = HuenoColors.Text, fontSize = HuenoType.BodySmall, lineHeight = HuenoType.BodyLine)
    }
}

@Composable
private fun OptionCard(
    title: String,
    subtitle: String,
    trailing: String?,
    icon: ImageVector?,
    filled: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val background = if (filled) HuenoColors.Action else HuenoColors.Surface
    val content = if (filled) HuenoColors.OnAction else HuenoColors.Text
    val secondary = if (filled) Color(0xFFE3EAFD) else HuenoColors.TextMuted
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(HuenoShapes.Large)
            .background(if (enabled) background else HuenoColors.Line)
            .then(if (filled) Modifier else Modifier.border(1.dp, HuenoColors.Line, HuenoShapes.Large))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val shown = if (enabled) content else HuenoColors.Text
        icon?.let { Icon(it, contentDescription = null, tint = shown, modifier = Modifier.size(28.dp)) }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, color = shown, fontSize = HuenoType.Title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = if (enabled) secondary else HuenoColors.Text, fontSize = HuenoType.BodySmall)
        }
        trailing?.let { Text(it, color = shown, fontSize = HuenoType.Title, fontWeight = FontWeight.Bold) }
    }
}
