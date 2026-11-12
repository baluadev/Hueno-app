package com.hueno.coloridentifier.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hueno.coloridentifier.BuildConfig
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.camera.CameraScreen
import com.hueno.coloridentifier.ui.history.HistoryScreen
import com.hueno.coloridentifier.ui.onboarding.OnboardingScreen
import com.hueno.coloridentifier.ui.photo.PhotoScreen
import com.hueno.coloridentifier.ui.pro.ProScreen
import com.hueno.monetization.BillingEvent
import com.hueno.coloridentifier.ui.result.ResultScreen
import com.hueno.coloridentifier.ui.settings.SettingsScreen

@Composable
fun HuenoApp(viewModel: HuenoViewModel = viewModel()) {
    // Screens follow the system's light or dark setting. The camera is always dark, so the preview
    // is not framed by bright glare and the eye judges its colors against a neutral dark surround.
    val systemDark = isSystemInDarkTheme()
    if (viewModel.showOnboarding) {
        SystemBarIcons(darkScreen = systemDark)
        HuenoTheme { OnboardingScreen(onDone = viewModel::finishOnboarding) }
        return
    }

    val tab = viewModel.tab
    val dark = tab == Tab.CAMERA || systemDark
    val speaker = rememberSpeaker()
    val money by viewModel.money.collectAsStateWithLifecycle()
    val activity = LocalContext.current as? Activity
    val bannerUnitId = if (money.showBanners) viewModel.bannerUnitId else null
    // The detail, settings and Pro screens follow the system setting and cover the whole window.
    val overlayOpen = viewModel.openCapture != null || viewModel.proScreen != null || viewModel.showSettings
    SystemBarIcons(darkScreen = if (overlayOpen) systemDark else dark)

    LaunchedEffect(Unit) { activity?.let(viewModel::onMainScreenShown) }

    // Messages for the Pro screen. Kept here so a purchase that completes after the screen was
    // closed is still announced, as a toast.
    var proMessage by remember { mutableStateOf<String?>(null) }
    val appContext = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        viewModel.billingEvents.collect { event ->
            val text = appContext.getString(event.messageRes())
            if (viewModel.proScreen != null) proMessage = text
            else Toast.makeText(appContext, text, Toast.LENGTH_LONG).show()
        }
    }

    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val exportDone = stringResource(R.string.export_done)
    val exportFailed = stringResource(R.string.export_failed)
    // The system file picker chooses where the CSV goes, so no storage permission is needed.
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            viewModel.exportCsv(uri) { ok ->
                Toast.makeText(context, if (ok) exportDone else exportFailed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Moving between the dark camera and a light tab fades the colors over instead of flashing.
    val darkness by animateFloatAsState(
        targetValue = if (dark) 1f else 0f,
        animationSpec = tween(durationMillis = TAB_FADE_MILLIS),
        label = "darkness",
    )
    HuenoTheme(darkness = darkness) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Crossfade(
                    targetState = tab,
                    animationSpec = tween(durationMillis = TAB_FADE_MILLIS),
                    modifier = Modifier.weight(1f),
                    label = "tab",
                ) { shown ->
                    when (shown) {
                        Tab.CAMERA -> CameraScreen(
                            speaker = speaker,
                            compareReference = viewModel.compareReference,
                            onCompareReference = viewModel::changeCompareReference,
                            onCapture = viewModel::captureFromCamera,
                            canSaveProfile = viewModel::canSaveProfile,
                            onProfileLimit = viewModel::onProfileLimit,
                            onOpenSettings = viewModel::openSettings,
                        )
                        Tab.PHOTO -> PhotoScreen(onSave = viewModel::captureFromPhoto)
                        Tab.HISTORY -> {
                            val captures by viewModel.captures.collectAsStateWithLifecycle()
                            val filter = viewModel.selectedCollection
                            HistoryScreen(
                                captures = captures.filter { filter == null || it.collection == filter },
                                collections = collections,
                                selectedCollection = filter,
                                isPro = money.isPro,
                                bannerUnitId = bannerUnitId,
                                onOpen = viewModel::open,
                                onSelectCollection = viewModel::selectCollection,
                                onExport = { if (viewModel.canExport()) exportLauncher.launch(EXPORT_FILE_NAME) },
                                onOpenPro = viewModel::openPro,
                            )
                        }
                    }
                }
                HuenoNavigationBar(tab, viewModel::selectTab)
            }
        }
    }

    // The detail screen is drawn over the tab instead of replacing it, so the camera session and
    // its calibration stay alive while a captured color is being read.
    viewModel.openCapture?.let { capture ->
        BackHandler { viewModel.closeCapture() }
        HuenoTheme {
            ResultScreen(
                capture = capture,
                collections = collections,
                speaker = speaker,
                onBack = viewModel::closeCapture,
                onCompare = { viewModel.compareWithCamera(capture) },
                onDelete = { viewModel.delete(capture) },
                onRename = { viewModel.rename(capture, it) },
                onMove = { viewModel.moveToCollection(capture, it) },
                hexWithHash = viewModel.hexWithHash,
                bannerUnitId = bannerUnitId,
            )
        }
    }

    if (viewModel.showSettings) {
        BackHandler { viewModel.closeSettings() }
        val feedbackSubject = stringResource(R.string.settings_feedback_subject)
        HuenoTheme {
            SettingsScreen(
                money = money,
                hexWithHash = viewModel.hexWithHash,
                versionName = BuildConfig.VERSION_NAME,
                onHexWithHash = viewModel::changeHexWithHash,
                onOpenPro = viewModel::openPro,
                onPrivacy = { activity?.let(viewModel::showPrivacyOptions) },
                onFeedback = viewModel.feedbackEmail?.let { address ->
                    {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                            .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                            .putExtra(Intent.EXTRA_SUBJECT, feedbackSubject)
                        // No mail app installed is not an error worth showing; the row just does nothing.
                        runCatching { context.startActivity(intent) }
                    }
                },
                onClose = viewModel::closeSettings,
            )
        }
    }

    viewModel.proScreen?.let { reason ->
        BackHandler { viewModel.closePro() }
        LaunchedEffect(Unit) { proMessage = null }
        val unavailable = stringResource(R.string.pro_buy_unavailable)
        val granted = stringResource(R.string.pro_watch_granted)
        val noVideo = stringResource(R.string.pro_watch_failed)
        val restoring = stringResource(R.string.pro_restore_checking)
        HuenoTheme {
            ProScreen(
                reason = reason,
                state = money,
                rewardedBusy = viewModel.rewardedBusy,
                message = proMessage,
                onClose = viewModel::closePro,
                onBuy = {
                    proMessage = null
                    activity?.let { if (!viewModel.buyPro(it)) proMessage = unavailable }
                },
                onWatch = {
                    activity?.let { host ->
                        proMessage = null
                        viewModel.watchRewarded(host) { ok -> proMessage = if (ok) granted else noVideo }
                    }
                },
                onRestore = {
                    proMessage = restoring
                    viewModel.restorePurchases()
                },
                onPrivacy = { activity?.let(viewModel::showPrivacyOptions) },
            )
        }
    }
}

private const val EXPORT_FILE_NAME = "hueno-colors.csv"
private const val TAB_FADE_MILLIS = 280

private fun BillingEvent.messageRes(): Int = when (this) {
    BillingEvent.PURCHASED -> R.string.pro_event_purchased
    BillingEvent.PENDING -> R.string.pro_event_pending
    BillingEvent.PURCHASE_FAILED -> R.string.pro_event_failed
    BillingEvent.RESTORED -> R.string.pro_event_restored
    BillingEvent.NOTHING_TO_RESTORE -> R.string.pro_event_nothing
    BillingEvent.RESTORE_FAILED -> R.string.pro_event_restore_failed
}

/** Keeps status and navigation bar icons readable: light icons on dark screens and the reverse. */
@Composable
private fun SystemBarIcons(darkScreen: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkScreen
            isAppearanceLightNavigationBars = !darkScreen
        }
    }
}

@Composable
private fun HuenoNavigationBar(current: Tab, onSelect: (Tab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        NavItem(current, Tab.CAMERA, HuenoIcons.Camera, stringResource(R.string.tab_camera), onSelect)
        NavItem(current, Tab.PHOTO, HuenoIcons.Photo, stringResource(R.string.tab_photo), onSelect)
        NavItem(current, Tab.HISTORY, HuenoIcons.History, stringResource(R.string.tab_history), onSelect)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.NavItem(
    current: Tab,
    tab: Tab,
    icon: ImageVector,
    label: String,
    onSelect: (Tab) -> Unit,
) {
    NavigationBarItem(
        selected = current == tab,
        onClick = { onSelect(tab) },
        icon = { Icon(icon, contentDescription = null) },
        label = { Text(label) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onBackground,
            selectedTextColor = MaterialTheme.colorScheme.onBackground,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            indicatorColor = if (current == tab) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        ),
    )
}
