package com.hueno.coloridentifier.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.AdBanner
import com.hueno.coloridentifier.ui.Capture
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoSecondaryButton
import com.hueno.coloridentifier.ui.HuenoSize
import com.hueno.coloridentifier.ui.TabTitle
import com.hueno.coloridentifier.ui.relativeTime
import com.hueno.coloridentifier.ui.HuenoShapes
import com.hueno.coloridentifier.ui.HuenoType
import com.hueno.coloridentifier.ui.ThumbnailImage

/**
 * Saved colors, newest first, with an optional collection filter.
 *
 * [captures] is already filtered by [selectedCollection]; [collections] lists every collection
 * that has at least one color.
 */
@Composable
fun HistoryScreen(
    captures: List<Capture>,
    collections: List<String>,
    selectedCollection: String?,
    isPro: Boolean,
    bannerUnitId: String?,
    onOpen: (Capture) -> Unit,
    onSelectCollection: (String?) -> Unit,
    onExport: () -> Unit,
    onOpenPro: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = HuenoSize.ScreenPadding, end = 12.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(Modifier.weight(1f)) {
                TabTitle(stringResource(R.string.history_title))
                Text(
                    pluralStringResource(R.plurals.history_count, captures.size, captures.size),
                    color = HuenoColors.TextMuted,
                    fontSize = HuenoType.Label,
                )
            }
            HuenoSecondaryButton(stringResource(R.string.history_export), onExport, enabled = captures.isNotEmpty())
            HuenoSecondaryButton(stringResource(if (isPro) R.string.pro_open_active else R.string.pro_open), onOpenPro)
        }

        if (collections.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterPill(stringResource(R.string.history_all), selectedCollection == null) { onSelectCollection(null) }
                collections.forEach { name ->
                    FilterPill(name, selectedCollection == name) { onSelectCollection(name) }
                }
            }
        }

        if (captures.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    stringResource(R.string.history_empty_title),
                    color = HuenoColors.Text,
                    fontSize = HuenoType.Title,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.history_empty_body), color = HuenoColors.TextMuted, fontSize = HuenoType.Body)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(captures, key = { it.id }) { capture -> CaptureRow(capture) { onOpen(capture) } }
            }
        }
        bannerUnitId?.let { AdBanner(it, Modifier.padding(vertical = 6.dp)) }
    }
}

/** A filter choice. The selected one is filled, so the state does not rely on color alone. */
@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .heightIn(min = 44.dp)
            .clip(HuenoShapes.Large)
            .background(if (selected) HuenoColors.Text else Color.Transparent)
            .then(
                if (selected) Modifier else Modifier.border(1.dp, HuenoColors.TextMuted, HuenoShapes.Large),
            )
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) HuenoColors.Background else HuenoColors.Text,
            fontSize = HuenoType.BodySmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun CaptureRow(capture: Capture, onClick: () -> Unit) {
    val rgb = capture.rgb
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(HuenoShapes.Medium)
            .background(HuenoColors.Surface)
            .border(1.dp, HuenoColors.Line, HuenoShapes.Medium)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(48.dp)) {
            val file = capture.thumbnail
            if (file == null) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(HuenoShapes.Medium)
                        .background(Color(rgb.r, rgb.g, rgb.b))
                        .border(1.dp, HuenoColors.Line, HuenoShapes.Medium),
                )
            } else {
                // The object on the left, with its color as a chip on the corner.
                ThumbnailImage(
                    file = file,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp).clip(HuenoShapes.Medium),
                )
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(22.dp)
                        .clip(HuenoShapes.Small)
                        .background(Color(rgb.r, rgb.g, rgb.b))
                        .border(2.dp, HuenoColors.Surface, HuenoShapes.Small),
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                capture.title,
                color = HuenoColors.Text,
                fontSize = HuenoType.Body,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // With a custom name on top, the line below carries the plain color name instead.
            val second = if (capture.label != null) capture.name.plain else capture.name.detail
            Text(
                stringResource(R.string.detail_and_hex, second, rgb.hex),
                color = HuenoColors.TextMuted,
                fontSize = HuenoType.Label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            relativeTime(capture.timeMillis, System.currentTimeMillis()),
            color = HuenoColors.TextMuted,
            fontSize = HuenoType.Caption,
        )
    }
}
