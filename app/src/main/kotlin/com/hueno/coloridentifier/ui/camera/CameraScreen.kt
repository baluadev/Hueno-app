package com.hueno.coloridentifier.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hueno.camera.CameraColorState
import com.hueno.camera.ColorCamera
import com.hueno.camera.SampleSize
import com.hueno.coloridentifier.BuildConfig
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.coloridentifier.ui.Speaker
import com.hueno.coloridentifier.ui.labelRes
import com.hueno.coloridentifier.ui.tipRes
import com.hueno.core.color.ColorDifference
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.ColorReading
import com.hueno.core.color.Rgb
import com.hueno.core.color.Verdict
import com.hueno.core.color.ConfidenceLevel

@Composable
fun CameraScreen(
    speaker: Speaker,
    compareReference: Rgb?,
    onCompareReference: (Rgb?) -> Unit,
    onCapture: (ColorReading) -> Unit,
) {
    val context = LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }

    if (granted) {
        LiveCamera(speaker, compareReference, onCompareReference, onCapture)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        ) {
            Text(
                stringResource(R.string.permission_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.permission_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuenoColors.Action,
                    contentColor = HuenoColors.OnAction,
                ),
            ) { Text(stringResource(R.string.permission_allow), fontSize = 17.sp) }
        }
    }
}

@Composable
private fun LiveCamera(
    speaker: Speaker,
    compareReference: Rgb?,
    onCompareReference: (Rgb?) -> Unit,
    onCapture: (ColorReading) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camera = remember { ColorCamera(context) }
    val previewView = remember {
        PreviewView(context).apply {
            // A TextureView-backed preview can be read back as a bitmap, which Freeze needs.
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val state by camera.state.collectAsStateWithLifecycle()

    var size by remember { mutableStateOf(SampleSize.MEDIUM) }
    var torch by remember { mutableStateOf(false) }
    var frozenFrame by remember { mutableStateOf<Bitmap?>(null) }
    var calibrating by remember { mutableStateOf(false) }
    val frozen = frozenFrame != null

    DisposableEffect(lifecycleOwner) {
        camera.start(lifecycleOwner, previewView)
        onDispose { camera.shutdown() }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            torch = torch,
            calibrated = state.reading?.calibrated == true,
            onTorch = {
                torch = !torch
                camera.setTorch(torch)
            },
            onCalibrate = { calibrating = true },
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(HuenoColors.InkRaised),
        ) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            frozenFrame?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
            // Drawn against the shorter side of the preview. The preview is center-cropped, so
            // the ring matches the sampled area closely but not to the pixel.
            Canvas(Modifier.fillMaxSize()) {
                val radius = (minOf(this.size.width, this.size.height) * size.radiusFraction).toFloat()
                val center = Offset(this.size.width / 2, this.size.height / 2)
                drawCircle(HuenoColors.Ink, radius + 4f, center, style = Stroke(width = 5f))
                drawCircle(Color.White, radius, center, style = Stroke(width = 5f))
                drawCircle(Color.White, 3f, center)
            }
            ConfidenceChip(state, frozen, Modifier.align(Alignment.TopStart).padding(12.dp))
            SizeSelector(
                selected = size,
                onSelect = {
                    size = it
                    camera.sampleSize = it
                },
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
            )
        }

        if (calibrating) {
            CalibrationPanel(
                calibrated = state.reading?.calibrated == true,
                error = state.error,
                onCalibrate = {
                    camera.sampleSize = SampleSize.LARGE
                    size = SampleSize.LARGE
                    camera.calibrate()
                    calibrating = false
                },
                onClear = {
                    camera.clearCalibration()
                    calibrating = false
                },
                onCancel = { calibrating = false },
            )
        } else {
            val current = state.reading
            if (compareReference != null && current != null) {
                ComparePanel(reference = compareReference, current = current.rgb) { speaker.say(it) }
            } else {
                ReadingRow(current) { current?.let { speaker.say(it.name.plain) } }
            }
            state.error?.let {
                Text(
                    it,
                    color = HuenoColors.OnInk,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            if (BuildConfig.DEBUG) DebugLine(state)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 36.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabeledAction(
                    icon = if (frozen) HuenoIcons.Play else HuenoIcons.Pause,
                    label = stringResource(if (frozen) R.string.camera_resume else R.string.camera_freeze),
                ) {
                    if (frozen) {
                        frozenFrame = null
                        camera.frozen = false
                    } else {
                        camera.frozen = true
                        frozenFrame = previewView.bitmap
                    }
                }
                CaptureButton(enabled = state.reading != null) { state.reading?.let(onCapture) }
                val comparing = compareReference != null
                LabeledAction(
                    icon = if (comparing) HuenoIcons.Close else HuenoIcons.Compare,
                    label = stringResource(if (comparing) R.string.compare_end else R.string.compare_start),
                ) {
                    // Starting a comparison takes the color in the ring as color A.
                    onCompareReference(if (comparing) null else state.reading?.rgb)
                }
            }
        }
    }
}

@Composable
private fun TopBar(torch: Boolean, calibrated: Boolean, onTorch: () -> Unit, onCalibrate: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.app_name),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = HuenoColors.OnInk,
            modifier = Modifier.weight(1f),
        )
        RoundIconButton(HuenoIcons.Calibrate, stringResource(R.string.camera_calibrate), calibrated, onCalibrate)
        Spacer(Modifier.width(6.dp))
        RoundIconButton(HuenoIcons.Torch, stringResource(R.string.camera_torch), torch, onTorch)
    }
}

/** A 48 dp round button. [active] inverts it, so the state does not depend on color alone. */
@Composable
private fun RoundIconButton(icon: ImageVector, description: String, active: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (active) HuenoColors.OnInk else HuenoColors.InkRaised)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                selected = active
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = if (active) HuenoColors.Ink else HuenoColors.OnInk)
    }
}

@Composable
private fun ConfidenceChip(state: CameraColorState, frozen: Boolean, modifier: Modifier = Modifier) {
    val reading = state.reading ?: return
    val level = reading.confidence.level
    val text = buildString {
        append(stringResource(level.labelRes()))
        if (reading.calibrated) append(" · ").append(stringResource(R.string.confidence_calibrated))
        if (frozen) append(" · ").append(stringResource(R.string.camera_frozen))
    }
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HuenoColors.Ink)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Icon shape carries the level as well as the words.
        Icon(
            if (level == ConfidenceLevel.GOOD) HuenoIcons.Check else HuenoIcons.Alert,
            contentDescription = null,
            tint = HuenoColors.OnInk,
            modifier = Modifier.size(16.dp),
        )
        Text(text, color = HuenoColors.OnInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SizeSelector(selected: SampleSize, onSelect: (SampleSize) -> Unit, modifier: Modifier = Modifier) {
    val options = listOf(
        Triple(SampleSize.SMALL, R.string.sample_small_short, R.string.sample_small),
        Triple(SampleSize.MEDIUM, R.string.sample_medium_short, R.string.sample_medium),
        Triple(SampleSize.LARGE, R.string.sample_large_short, R.string.sample_large),
    )
    Row(
        modifier = modifier.clip(RoundedCornerShape(24.dp)).background(HuenoColors.Ink).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEach { (option, short, long) ->
            val isSelected = option == selected
            val description = stringResource(long)
            Box(
                modifier = Modifier
                    .size(width = 48.dp, height = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) HuenoColors.OnInk else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(option) }
                    .semantics {
                        contentDescription = description
                        this.selected = isSelected
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(short),
                    color = if (isSelected) HuenoColors.Ink else HuenoColors.OnInkMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun ReadingRow(reading: ColorReading?, onSpeak: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(reading?.rgb?.let { Color(it.r, it.g, it.b) } ?: HuenoColors.InkRaised)
                .border(2.dp, HuenoColors.OnInk, RoundedCornerShape(16.dp)),
        )
        Column(Modifier.weight(1f)) {
            Text(
                reading?.name?.plain ?: stringResource(R.string.camera_starting),
                color = HuenoColors.OnInk,
                fontSize = if (reading == null) 22.sp else 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 36.sp,
            )
            if (reading != null) {
                Text(
                    stringResource(R.string.detail_and_hex, reading.name.detail, reading.rgb.hex),
                    color = HuenoColors.OnInkMuted,
                    fontSize = 15.sp,
                )
                reading.confidence.issue?.let {
                    Text(stringResource(it.tipRes()), color = HuenoColors.OnInk, fontSize = 13.sp)
                }
            }
        }
        if (reading != null) {
            RoundIconButton(HuenoIcons.Speak, stringResource(R.string.camera_speak), active = false, onClick = onSpeak)
        }
    }
}

@Composable
private fun LabeledAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .size(width = 72.dp, height = 60.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = HuenoColors.OnInk)
        Text(label, color = HuenoColors.OnInk, fontSize = 12.sp)
    }
}

@Composable
private fun CaptureButton(enabled: Boolean, onClick: () -> Unit) {
    val description = stringResource(R.string.camera_capture)
    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .border(4.dp, HuenoColors.OnInk, CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(10.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(if (enabled) HuenoColors.OnInk else HuenoColors.OnInkMuted),
        )
    }
}

@Composable
private fun CalibrationPanel(
    calibrated: Boolean,
    error: String?,
    onCalibrate: () -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            stringResource(R.string.calibrate_title),
            color = HuenoColors.OnInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(stringResource(R.string.calibrate_body), color = HuenoColors.OnInkMuted, fontSize = 15.sp)
        Text(stringResource(R.string.calibrate_note), color = HuenoColors.OnInkMuted, fontSize = 13.sp)
        error?.let { Text(it, color = HuenoColors.OnInk, fontSize = 14.sp) }
        Button(
            onClick = onCalibrate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HuenoColors.Action,
                contentColor = HuenoColors.OnAction,
            ),
        ) { Text(stringResource(R.string.calibrate_now), fontSize = 17.sp, fontWeight = FontWeight.SemiBold) }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f).height(48.dp)) {
                Text(stringResource(R.string.calibrate_cancel), color = HuenoColors.OnInk)
            }
            if (calibrated) {
                OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(stringResource(R.string.calibrate_clear), color = HuenoColors.OnInk)
                }
            }
        }
    }
}

/** Live comparison: color A on the left, the color in the ring (B) on the right, then the verdict. */
@Composable
private fun ComparePanel(reference: Rgb, current: Rgb, onSpeak: (String) -> Unit) {
    val result = remember(reference, current) { ColorDifference.compare(reference, current) }
    val nameA = remember(reference) { ColorNamer.name(reference).plain }
    val nameB = remember(current) { ColorNamer.name(current).plain }
    val verdict = stringResource(
        when (result.verdict) {
            Verdict.SAME -> R.string.compare_same
            Verdict.CLOSE -> R.string.compare_close
            Verdict.DIFFERENT -> R.string.compare_different
        },
    )
    // Each verdict has its own symbol, so it never depends on color.
    val icon = when (result.verdict) {
        Verdict.SAME -> HuenoIcons.Check
        Verdict.CLOSE -> HuenoIcons.Approx
        Verdict.DIFFERENT -> HuenoIcons.NotEqual
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompareSwatch(stringResource(R.string.compare_a), reference, stringResource(R.string.compare_swatch_a, nameA))
        CompareSwatch(stringResource(R.string.compare_b), current, stringResource(R.string.compare_swatch_b, nameB))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(icon, contentDescription = null, tint = HuenoColors.OnInk, modifier = Modifier.size(28.dp))
                Text(verdict, color = HuenoColors.OnInk, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Text(result.explanation, color = HuenoColors.OnInk, fontSize = 14.sp, lineHeight = 18.sp)
        }
        RoundIconButton(HuenoIcons.Speak, stringResource(R.string.camera_speak), active = false) {
            onSpeak("$verdict. ${result.explanation}")
        }
    }
}

@Composable
private fun CompareSwatch(letter: String, rgb: Rgb, description: String) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(rgb.r, rgb.g, rgb.b))
            .border(2.dp, HuenoColors.OnInk, RoundedCornerShape(14.dp))
            .semantics { contentDescription = description },
        contentAlignment = Alignment.TopStart,
    ) {
        Text(
            letter,
            color = HuenoColors.Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(4.dp)
                .clip(CircleShape)
                .background(HuenoColors.OnInk)
                .padding(horizontal = 6.dp),
        )
    }
}

/** Measurement numbers for tuning on real devices. Debug builds only. */
@Composable
private fun DebugLine(state: CameraColorState) {
    val reading = state.reading ?: return
    Text(
        "ring %.3f  jitter %.3f  fps %.0f  first %s ms  locked %s".format(
            reading.linear.luminance,
            reading.jitter,
            state.analyzedFps,
            state.firstReadingMs?.toString() ?: "-",
            if (state.locked) "yes" else "no",
        ),
        color = HuenoColors.OnInkMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}
