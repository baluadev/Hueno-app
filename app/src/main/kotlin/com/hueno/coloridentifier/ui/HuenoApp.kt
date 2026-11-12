package com.hueno.coloridentifier.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.camera.CameraScreen
import com.hueno.coloridentifier.ui.history.HistoryScreen
import com.hueno.coloridentifier.ui.onboarding.OnboardingScreen
import com.hueno.coloridentifier.ui.photo.PhotoScreen
import com.hueno.coloridentifier.ui.result.ResultScreen

@Composable
fun HuenoApp(viewModel: HuenoViewModel = viewModel()) {
    if (viewModel.showOnboarding) {
        SystemBarIcons(darkScreen = false)
        HuenoTheme(dark = false) { OnboardingScreen(onDone = viewModel::finishOnboarding) }
        return
    }

    val tab = viewModel.tab
    val dark = tab != Tab.HISTORY
    val speaker = rememberSpeaker()
    // The detail screen is light and covers the whole window while it is open.
    SystemBarIcons(darkScreen = dark && viewModel.openCapture == null)

    HuenoTheme(dark = dark) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                Box(Modifier.weight(1f)) {
                    when (tab) {
                        Tab.CAMERA -> CameraScreen(
                            speaker = speaker,
                            compareReference = viewModel.compareReference,
                            onCompareReference = viewModel::changeCompareReference,
                            onCapture = viewModel::captureFromCamera,
                        )
                        Tab.PHOTO -> PhotoScreen(onSave = viewModel::captureFromPhoto)
                        Tab.HISTORY -> {
                            val captures by viewModel.captures.collectAsStateWithLifecycle()
                            HistoryScreen(captures = captures, onOpen = viewModel::open)
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
        HuenoTheme(dark = false) {
            ResultScreen(
                capture = capture,
                speaker = speaker,
                onBack = viewModel::closeCapture,
                onCompare = { viewModel.compareWithCamera(capture) },
                onDelete = { viewModel.delete(capture) },
            )
        }
    }
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
