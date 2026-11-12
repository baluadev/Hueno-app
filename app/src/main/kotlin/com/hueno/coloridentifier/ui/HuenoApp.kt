package com.hueno.coloridentifier.ui

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hueno.coloridentifier.R
import com.hueno.coloridentifier.ui.camera.CameraScreen
import com.hueno.coloridentifier.ui.history.HistoryScreen
import com.hueno.coloridentifier.ui.photo.PhotoScreen
import com.hueno.coloridentifier.ui.result.ResultScreen

@Composable
fun HuenoApp(viewModel: HuenoViewModel = viewModel()) {
    val tab = viewModel.tab
    val dark = tab != Tab.HISTORY
    val speaker = rememberSpeaker()

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
