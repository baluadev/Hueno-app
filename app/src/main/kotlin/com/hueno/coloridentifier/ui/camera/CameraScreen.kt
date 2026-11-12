package com.hueno.coloridentifier.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hueno.camera.CalibrationProfile
import com.hueno.camera.CalibrationProfileStore
import com.hueno.camera.CameraColorState
import com.hueno.camera.CameraIssue
import com.hueno.camera.ColorCamera
import com.hueno.camera.SampleSize
import com.hueno.coloridentifier.BuildConfig
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.HuenoColors
import com.hueno.coloridentifier.ui.HuenoIcons
import com.hueno.coloridentifier.ui.Speaker
import com.hueno.coloridentifier.ui.Thumbnails
import com.hueno.coloridentifier.ui.labelRes
import com.hueno.coloridentifier.ui.tipRes
import com.hueno.core.color.ColorDifference
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.ColorReading
import com.hueno.core.color.ConfidenceLevel
import com.hueno.core.color.Rgb
import com.hueno.core.color.Verdict
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlin.math.min

private const val MAX_PROFILE_NAME = 40
private const val NOTICE_MILLIS = 8_000L

/**
 * @param onCapture receives the reading and, when available, a small picture of the object.
 * @param canSaveProfile whether one more calibration may be saved, given how many exist.
 * @param onProfileLimit called when a free user tries to save more calibrations than allowed.
 */
@Composable
fun CameraScreen(
    speaker: Speaker,
    compareReference: Rgb?,
    onCompareReference: (Rgb?) -> Unit,
    onCapture: (ColorReading, Bitmap?) -> Unit,
    canSaveProfile: (Int) -> Boolean,
    onProfileLimit: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun hasCamera() =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    var granted by remember { mutableStateOf(hasCamera()) }
    // After a refusal Android may stop showing its dialog. Only then does the button lead to the
    // app's system settings, because that is the one place left to turn the camera on.
    var blocked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        val activity = context.findActivity()
        blocked = !ok && activity != null &&
            !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)
    }
    // Picks up a permission turned on in system settings when the user comes back.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !granted && hasCamera()) granted = true
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (granted) {
        LiveCamera(speaker, compareReference, onCompareReference, onCapture, canSaveProfile, onProfileLimit, onOpenSettings)
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
        ) {
            Text(
                stringResource(R.string.permission_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = HuenoColors.OnInk,
            )
            Text(
                stringResource(if (blocked) R.string.permission_blocked_body else R.string.permission_body),
                style = MaterialTheme.typography.bodyLarge,
                color = HuenoColors.OnInkMuted,
            )
            Button(
                onClick = {
                    if (blocked) {
                        val intent = Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null),
                        )
                        runCatching { context.startActivity(intent) }
                    } else {
                        launcher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HuenoColors.Action,
                    contentColor = HuenoColors.OnAction,
                ),
            ) {
                Text(
                    stringResource(if (blocked) R.string.permission_open_settings else R.string.permission_allow),
                    fontSize = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun LiveCamera(
    speaker: Speaker,
    compareReference: Rgb?,
    onCompareReference: (Rgb?) -> Unit,
    onCapture: (ColorReading, Bitmap?) -> Unit,
    canSaveProfile: (Int) -> Boolean,
    onProfileLimit: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val camera = remember { ColorCamera(context) }
    val profileStore = remember { CalibrationProfileStore(context) }
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
    var calibrating by remember { mutableStateOf(false) }
    var profiles by remember { mutableStateOf(profileStore.all()) }
    var namingProfile by remember { mutableStateOf(false) }

    // Freeze: the picture on screen is held, and the ring can be moved around inside it.
    var frozenFrame by remember { mutableStateOf<Bitmap?>(null) }
    var ring by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var frozenReading by remember { mutableStateOf<ColorReading?>(null) }
    var box by remember { mutableStateOf(IntSize.Zero) }
    val frozen = frozenFrame != null
    val reading = if (frozen) frozenReading ?: state.reading else state.reading

    fun moveRing(position: Offset) {
        val frame = frozenFrame ?: return
        if (box.width == 0 || box.height == 0) return
        ring = Offset((position.x / box.width).coerceIn(0f, 1f), (position.y / box.height).coerceIn(0f, 1f))
        frozenReading = camera.readingFromPixels(pixelsAround(frame, ring, size.radiusFraction))
    }

    /** Drops the calibration for [reason] and re-reads a frozen spot without it. */
    fun dropCalibration(reason: CameraIssue) {
        if (!camera.isCalibrated) return
        camera.clearCalibration(reason)
        frozenFrame?.let { frozenReading = camera.readingFromPixels(pixelsAround(it, ring, size.radiusFraction)) }
    }

    DisposableEffect(lifecycleOwner) {
        camera.start(lifecycleOwner, previewView)
        // The camera is closed while the app is in the background, and the light may well have
        // changed by the time it is back, so a calibration is not trusted across that gap.
        // The torch goes off with the camera too.
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                dropCalibration(CameraIssue.CALIBRATION_CLEARED_PAUSED)
                torch = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            camera.shutdown()
        }
    }

    // "Calibration was cleared" is a notice, not a lasting state; it would otherwise stay for as
    // long as the camera is used.
    LaunchedEffect(state.issue) {
        val issue = state.issue
        if (issue == CameraIssue.CALIBRATION_CLEARED_PAUSED || issue == CameraIssue.CALIBRATION_CLEARED_TORCH) {
            delay(NOTICE_MILLIS)
            camera.dismissIssue(issue)
        }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(
            torch = torch,
            calibrated = state.reading?.calibrated == true,
            onTorch = {
                torch = !torch
                camera.setTorch(torch)
                // The torch changes the light the calibration was measured under.
                dropCalibration(CameraIssue.CALIBRATION_CLEARED_TORCH)
            },
            onCalibrate = { calibrating = true },
            onSettings = onOpenSettings,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(HuenoColors.InkRaised),
        ) {
            // The picture and its touch handling are one layer; the chips above are siblings, so
            // pressing them never moves the ring.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { box = it }
                    .pointerInput(frozen) {
                        if (!frozen) return@pointerInput
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            moveRing(down.position)
                            do {
                                val event = awaitPointerEvent()
                                event.changes.firstOrNull { it.pressed }?.let { moveRing(it.position) }
                                event.changes.forEach { it.consume() }
                            } while (event.changes.any { it.pressed })
                        }
                    },
            ) {
                AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
                frozenFrame?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds,
                    )
                }
                // Drawn against the shorter side of the preview. The preview is center-cropped, so
                // the live ring matches the sampled area closely but not to the pixel.
                Canvas(Modifier.fillMaxSize()) {
                    val radius = (minOf(this.size.width, this.size.height) * size.radiusFraction).toFloat()
                    val center = if (frozen) {
                        Offset(this.size.width * ring.x, this.size.height * ring.y)
                    } else {
                        Offset(this.size.width / 2, this.size.height / 2)
                    }
                    drawCircle(HuenoColors.Ink, radius + 4f, center, style = Stroke(width = 5f))
                    drawCircle(Color.White, radius, center, style = Stroke(width = 5f))
                    drawCircle(Color.White, 3f, center)
                }
            }
            // One row, so a long chip wraps onto a second line instead of sliding under the selector.
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                ConfidenceChip(reading, frozen, Modifier.weight(1f, fill = false).padding(end = 8.dp))
                SizeSelector(
                    selected = size,
                    onSelect = {
                        size = it
                        camera.sampleSize = it
                        frozenFrame?.let { frame ->
                            frozenReading = camera.readingFromPixels(pixelsAround(frame, ring, it.radiusFraction))
                        }
                    },
                )
            }
        }

        if (calibrating) {
            CalibrationPanel(
                calibrated = state.reading?.calibrated == true,
                supportsProfiles = state.supportsProfiles,
                profiles = profiles,
                issue = state.issue,
                onCalibrate = {
                    camera.sampleSize = SampleSize.LARGE
                    size = SampleSize.LARGE
                    camera.calibrate()
                },
                onSave = { if (canSaveProfile(profiles.size)) namingProfile = true else onProfileLimit() },
                onUse = {
                    camera.applyProfile(it)
                    calibrating = false
                },
                onDelete = { profiles = profileStore.delete(it.id) },
                onClear = {
                    camera.clearCalibration()
                    calibrating = false
                },
                onDone = { calibrating = false },
            )
        } else {
            if (compareReference != null && reading != null) {
                ComparePanel(reference = compareReference, current = reading.rgb) { speaker.say(it) }
            } else {
                ReadingRow(reading, frozen) { reading?.let { speaker.say(it.name.plain) } }
            }
            state.issue?.let {
                Text(
                    stringResource(it.messageRes()),
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
                        frozenReading = null
                        camera.frozen = false
                    } else {
                        camera.frozen = true
                        ring = Offset(0.5f, 0.5f)
                        frozenReading = null
                        frozenFrame = previewView.bitmap
                    }
                }
                CaptureButton(enabled = reading != null) {
                    reading?.let { current ->
                        // The picture shows what was in the ring: the frozen frame, or the live one.
                        val frame = frozenFrame ?: previewView.bitmap
                        val spot = if (frozen) ring else Offset(0.5f, 0.5f)
                        onCapture(current, frame?.let { Thumbnails.around(it, spot.x, spot.y) })
                    }
                }
                val comparing = compareReference != null
                LabeledAction(
                    icon = if (comparing) HuenoIcons.Close else HuenoIcons.Compare,
                    label = stringResource(if (comparing) R.string.compare_end else R.string.compare_start),
                ) {
                    // Starting a comparison takes the color in the ring as color A.
                    onCompareReference(if (comparing) null else reading?.rgb)
                }
            }
        }
    }

    if (namingProfile) {
        ProfileNameDialog(
            onDismiss = { namingProfile = false },
            onSave = { name ->
                namingProfile = false
                camera.snapshotProfile(name)?.let { profiles = profileStore.save(it) }
            },
        )
    }
}

/** Pixels inside a circle around a spot of the frozen picture, at most a few thousand. */
private fun pixelsAround(frame: Bitmap, spot: Offset, radiusFraction: Double): IntArray {
    val cx = (spot.x * (frame.width - 1)).toInt()
    val cy = (spot.y * (frame.height - 1)).toInt()
    val radius = max(1, (min(frame.width, frame.height) * radiusFraction).toInt())
    val step = max(1, radius / 24)
    val out = ArrayList<Int>()
    var dy = -radius
    while (dy <= radius) {
        var dx = -radius
        while (dx <= radius) {
            val x = cx + dx
            val y = cy + dy
            if (dx * dx + dy * dy <= radius * radius && x in 0 until frame.width && y in 0 until frame.height) {
                out.add(frame.getPixel(x, y))
            }
            dx += step
        }
        dy += step
    }
    return out.toIntArray()
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private fun CameraIssue.messageRes(): Int = when (this) {
    CameraIssue.START_FAILED -> R.string.camera_issue_start
    CameraIssue.LOCK_REJECTED -> R.string.camera_issue_lock
    CameraIssue.CALIBRATION_FAILED -> R.string.camera_issue_calibration
    CameraIssue.CALIBRATION_TOO_BRIGHT -> R.string.camera_issue_calibration_bright
    CameraIssue.CALIBRATION_TOO_DARK -> R.string.camera_issue_calibration_dark
    CameraIssue.PROFILE_FAILED -> R.string.camera_issue_profile
    CameraIssue.CALIBRATION_CLEARED_PAUSED -> R.string.camera_issue_cleared_paused
    CameraIssue.CALIBRATION_CLEARED_TORCH -> R.string.camera_issue_cleared_torch
}

@Composable
private fun TopBar(
    torch: Boolean,
    calibrated: Boolean,
    onTorch: () -> Unit,
    onCalibrate: () -> Unit,
    onSettings: () -> Unit,
) {
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
        Spacer(Modifier.width(6.dp))
        RoundIconButton(HuenoIcons.Settings, stringResource(R.string.settings_title), false, onSettings)
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
private fun ConfidenceChip(reading: ColorReading?, frozen: Boolean, modifier: Modifier = Modifier) {
    if (reading == null) return
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
private fun ReadingRow(reading: ColorReading?, frozen: Boolean, onSpeak: () -> Unit) {
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
                val tip = if (frozen) {
                    stringResource(R.string.camera_frozen_hint)
                } else {
                    reading.confidence.issue?.let { stringResource(it.tipRes()) }
                }
                tip?.let { Text(it, color = HuenoColors.OnInk, fontSize = 13.sp) }
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
    supportsProfiles: Boolean,
    profiles: List<CalibrationProfile>,
    issue: CameraIssue?,
    onCalibrate: () -> Unit,
    onSave: () -> Unit,
    onUse: (CalibrationProfile) -> Unit,
    onDelete: (CalibrationProfile) -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Close sits beside the title, so it stays in reach however long the list of saved places
        // grows; at the bottom it ended up below the fold once one place was saved.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(if (calibrated) R.string.calibrate_done_title else R.string.calibrate_title),
                color = HuenoColors.OnInk,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            RoundIconButton(HuenoIcons.Close, stringResource(R.string.calibrate_close_panel), active = false, onClick = onDone)
        }
        Text(
            stringResource(if (calibrated) R.string.calibrate_done_body else R.string.calibrate_body),
            color = HuenoColors.OnInkMuted,
            fontSize = 15.sp,
        )
        issue?.let { Text(stringResource(it.messageRes()), color = HuenoColors.OnInk, fontSize = 14.sp) }

        Button(
            onClick = onCalibrate,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HuenoColors.Action, contentColor = HuenoColors.OnAction),
        ) {
            Text(
                stringResource(if (calibrated) R.string.calibrate_again else R.string.calibrate_now),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        if (supportsProfiles) {
            if (calibrated) {
                OutlinedButton(onClick = onSave, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text(stringResource(R.string.profile_save), color = HuenoColors.OnInk)
                }
            }
            if (profiles.isNotEmpty()) {
                Text(
                    stringResource(R.string.profile_saved_heading),
                    color = HuenoColors.OnInkMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
                profiles.forEach { profile ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(HuenoColors.InkRaised)
                            .padding(start = 14.dp, end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(profile.name, color = HuenoColors.OnInk, fontSize = 16.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onUse(profile) }) {
                            Text(stringResource(R.string.profile_use), color = HuenoColors.OnInk, fontWeight = FontWeight.SemiBold)
                        }
                        val delete = stringResource(R.string.profile_delete, profile.name)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable(role = Role.Button) { onDelete(profile) }
                                .semantics { contentDescription = delete },
                            contentAlignment = Alignment.Center,
                        ) { Icon(HuenoIcons.Trash, contentDescription = null, tint = HuenoColors.OnInkMuted) }
                    }
                }
                Text(stringResource(R.string.profile_note), color = HuenoColors.OnInkMuted, fontSize = 13.sp)
            }
        } else {
            Text(stringResource(R.string.profile_unsupported), color = HuenoColors.OnInkMuted, fontSize = 13.sp)
        }

        if (calibrated) {
            OutlinedButton(onClick = onClear, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(stringResource(R.string.calibrate_clear), color = HuenoColors.OnInk)
            }
        }
    }
}

@Composable
private fun ProfileNameDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.profile_name_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.profile_name_body))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(MAX_PROFILE_NAME) },
                    singleLine = true,
                    label = { Text(stringResource(R.string.profile_name_label)) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(text.trim()) }, enabled = text.isNotBlank()) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.calibrate_cancel)) } },
    )
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
    // The verdict gets a line of its own: beside the swatches a narrow phone broke "Different"
    // in the middle of the word.
    Column(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, contentDescription = null, tint = HuenoColors.OnInk, modifier = Modifier.size(28.dp))
            Text(
                verdict,
                color = HuenoColors.OnInk,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            RoundIconButton(HuenoIcons.Speak, stringResource(R.string.camera_speak), active = false) {
                onSpeak("$verdict. ${result.explanation}")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CompareSwatch(stringResource(R.string.compare_a), reference, stringResource(R.string.compare_swatch_a, nameA))
            CompareSwatch(stringResource(R.string.compare_b), current, stringResource(R.string.compare_swatch_b, nameB))
            Text(
                result.explanation,
                color = HuenoColors.OnInk,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f),
            )
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
        "ring %.3f  jitter %.3f  fps %.0f  first %s ms  locked %s  profiles %s".format(
            reading.linear.luminance,
            reading.jitter,
            state.analyzedFps,
            state.firstReadingMs?.toString() ?: "-",
            if (state.locked) "yes" else "no",
            if (state.supportsProfiles) "yes" else "no",
        ),
        color = HuenoColors.OnInkMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        modifier = Modifier.padding(horizontal = 20.dp),
    )
}
