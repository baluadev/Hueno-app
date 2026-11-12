package com.hueno.coloridentifier.ui.history

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.Capture
import com.hueno.coloridentifier.ui.HuenoColors

@Composable
fun HistoryScreen(captures: List<Capture>, onOpen: (Capture) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                stringResource(R.string.history_title),
                color = HuenoColors.OnPaper,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            if (captures.isNotEmpty()) {
                Text(
                    stringResource(R.string.history_count, captures.size),
                    color = HuenoColors.OnPaperMuted,
                    fontSize = 13.sp,
                )
            }
        }

        if (captures.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    stringResource(R.string.history_empty_title),
                    color = HuenoColors.OnPaper,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(stringResource(R.string.history_empty_body), color = HuenoColors.OnPaperMuted, fontSize = 16.sp)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(captures, key = { it.id }) { capture -> CaptureRow(capture) { onOpen(capture) } }
            }
        }
    }
}

@Composable
private fun CaptureRow(capture: Capture, onClick: () -> Unit) {
    val rgb = capture.rgb
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(HuenoColors.Card)
            .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(16.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(rgb.r, rgb.g, rgb.b))
                .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(12.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(capture.name.plain, color = HuenoColors.OnPaper, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.detail_and_hex, capture.name.detail, rgb.hex),
                color = HuenoColors.OnPaperMuted,
                fontSize = 13.sp,
            )
        }
        Text(
            DateUtils.getRelativeTimeSpanString(
                capture.timeMillis,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
            ).toString(),
            color = HuenoColors.OnPaperMuted,
            fontSize = 12.sp,
        )
    }
}
