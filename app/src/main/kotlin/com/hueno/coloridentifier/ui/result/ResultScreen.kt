package com.hueno.coloridentifier.ui.result

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.AdBanner
import com.hueno.coloridentifier.ui.Capture
import com.hueno.coloridentifier.ui.CaptureSource
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.coloridentifier.ui.Speaker
import com.hueno.coloridentifier.ui.ThumbnailImage
import com.hueno.coloridentifier.ui.labelRes
import com.hueno.coloridentifier.ui.tipRes
import com.hueno.core.color.ColorMath
import com.hueno.core.color.ConfidenceLevel

/** Longest custom name or collection title accepted, matching what the data layer keeps. */
private const val MAX_TEXT = 60

@Composable
fun ResultScreen(
    capture: Capture,
    collections: List<String>,
    speaker: Speaker,
    onBack: () -> Unit,
    onCompare: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String?) -> Unit,
    onMove: (String?) -> Unit,
    hexWithHash: Boolean = true,
    bannerUnitId: String? = null,
) {
    val rgb = capture.rgb
    val (h, s, l) = ColorMath.toHsl(rgb)
    var renaming by remember { mutableStateOf(false) }
    var moving by remember { mutableStateOf(false) }

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
                    .clickable(role = Role.Button, onClick = onBack)
                    .semantics { contentDescription = back },
                contentAlignment = Alignment.Center,
            ) { Icon(HuenoIcons.Back, contentDescription = null, tint = HuenoColors.OnPaper) }
            Text(
                stringResource(R.string.result_title),
                color = HuenoColors.OnPaper,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        Column(
            modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(168.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(rgb.r, rgb.g, rgb.b))
                    // A hairline keeps very light colors visible against the paper background.
                    .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.BottomStart,
            ) {
                // The object the color came from, so the swatch can be matched to a real thing.
                capture.thumbnail?.let { file ->
                    ThumbnailImage(
                        file = file,
                        contentDescription = stringResource(R.string.thumbnail_description),
                        modifier = Modifier
                            .padding(12.dp)
                            .size(84.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(16.dp)),
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(
                        capture.name.plain,
                        color = HuenoColors.OnPaper,
                        fontSize = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        stringResource(R.string.result_closest, capture.name.detail),
                        color = HuenoColors.OnPaperMuted,
                        fontSize = 16.sp,
                    )
                }
                val speak = stringResource(R.string.camera_speak)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(HuenoColors.OnPaper)
                        .clickable(role = Role.Button) { speaker.say(capture.name.plain) }
                        .semantics { contentDescription = speak },
                    contentAlignment = Alignment.Center,
                ) { Icon(HuenoIcons.Speak, contentDescription = null, tint = HuenoColors.Paper) }
            }

            QualityNote(capture)

            Card {
                CodeRow(stringResource(R.string.result_hex), rgb.hex, copyValue = if (hexWithHash) rgb.hex else rgb.hex.removePrefix("#"))
                HorizontalDivider(color = HuenoColors.PaperLine)
                CodeRow(stringResource(R.string.result_rgb), stringResource(R.string.rgb_value, rgb.r, rgb.g, rgb.b))
                HorizontalDivider(color = HuenoColors.PaperLine)
                CodeRow(stringResource(R.string.result_hsl), stringResource(R.string.hsl_value, h, s, l))
            }

            Card {
                EditRow(
                    label = stringResource(R.string.result_your_name),
                    value = capture.label ?: stringResource(R.string.result_no_name),
                    action = stringResource(R.string.result_edit),
                ) { renaming = true }
                HorizontalDivider(color = HuenoColors.PaperLine)
                EditRow(
                    label = stringResource(R.string.result_collection),
                    value = capture.collection ?: stringResource(R.string.result_no_collection),
                    action = stringResource(R.string.result_change),
                ) { moving = true }
            }

            Button(
                onClick = onCompare,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuenoColors.Action,
                    contentColor = HuenoColors.OnAction,
                ),
            ) {
                Icon(HuenoIcons.Compare, contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(R.string.result_compare),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(HuenoIcons.Trash, contentDescription = null, tint = HuenoColors.OnPaper, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(R.string.result_delete),
                    color = HuenoColors.OnPaper,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            bannerUnitId?.let { AdBanner(it) }
        }
    }

    if (renaming) {
        RenameDialog(
            current = capture.label.orEmpty(),
            onDismiss = { renaming = false },
            onSave = {
                onRename(it)
                renaming = false
            },
        )
    }
    if (moving) {
        CollectionDialog(
            current = capture.collection,
            collections = collections,
            onDismiss = { moving = false },
            onSave = {
                onMove(it)
                moving = false
            },
        )
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HuenoColors.Card)
            .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(14.dp)),
    ) { content() }
}

@Composable
private fun QualityNote(capture: Capture) {
    val confidence = capture.confidence
    val (icon, title, detail) = if (capture.source == CaptureSource.PHOTO || confidence == null) {
        Triple(HuenoIcons.Check, null, stringResource(R.string.result_from_photo))
    } else {
        Triple(
            if (confidence.level == ConfidenceLevel.GOOD) HuenoIcons.Check else HuenoIcons.Alert,
            stringResource(confidence.level.labelRes()),
            confidence.issue?.let { stringResource(it.tipRes()) }
                ?: if (capture.calibrated) stringResource(R.string.confidence_calibrated) else null,
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(HuenoColors.Card)
            .border(1.dp, HuenoColors.PaperLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(icon, contentDescription = null, tint = HuenoColors.OnPaper, modifier = Modifier.size(20.dp))
        Column {
            title?.let { Text(it, color = HuenoColors.OnPaper, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
            detail?.let { Text(it, color = HuenoColors.OnPaper, fontSize = 14.sp) }
        }
    }
}

@Composable
private fun CodeRow(label: String, value: String, copyValue: String = value) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val copyLabel = stringResource(R.string.result_copy, label)
    val copied = stringResource(R.string.result_copied, label)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(role = Role.Button) {
                clipboard.setText(AnnotatedString(copyValue))
                Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
            }
            .semantics { contentDescription = "$copyLabel, $value" }
            .padding(start = 14.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            color = HuenoColors.OnPaperMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(48.dp),
        )
        Text(value, color = HuenoColors.OnPaper, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Icon(HuenoIcons.Copy, contentDescription = null, tint = HuenoColors.Action, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun EditRow(label: String, value: String, action: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = HuenoColors.OnPaperMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(value, color = HuenoColors.OnPaper, fontSize = 17.sp, fontWeight = FontWeight.Medium)
        }
        Text(action, color = HuenoColors.Action, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun RenameDialog(current: String, onDismiss: () -> Unit, onSave: (String?) -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.rename_body))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(MAX_TEXT) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.result_your_name)) },
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(text) }) { Text(stringResource(R.string.dialog_save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.calibrate_cancel)) } },
    )
}

/** Picks an existing collection, types a new one, or takes the color out of collections. */
@Composable
private fun CollectionDialog(
    current: String?,
    collections: List<String>,
    onDismiss: () -> Unit,
    onSave: (String?) -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    var newName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.collection_title)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ChoiceRow(stringResource(R.string.result_no_collection), selected == null && newName.isBlank()) {
                    selected = null
                    newName = ""
                }
                collections.forEach { name ->
                    ChoiceRow(name, selected == name && newName.isBlank()) {
                        selected = name
                        newName = ""
                    }
                }
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it.take(MAX_TEXT) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.collection_new)) },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        },
        // Typing a new name takes priority over the selected row.
        confirmButton = {
            TextButton(onClick = { onSave(newName.ifBlank { selected }) }) { Text(stringResource(R.string.dialog_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.calibrate_cancel)) } },
    )
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(label)
    }
}
