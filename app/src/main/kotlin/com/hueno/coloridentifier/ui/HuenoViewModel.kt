package com.hueno.coloridentifier.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hueno.core.color.ColorName
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.ColorReading
import com.hueno.core.color.Confidence
import com.hueno.core.color.Rgb
import com.hueno.data.CaptureRepository
import com.hueno.data.StoredCapture
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Tab { CAMERA, PHOTO, HISTORY }

enum class CaptureSource { CAMERA, PHOTO }

/** A saved color as the screens show it. The name is derived from the stored color. */
data class Capture(
    val id: Long,
    val rgb: Rgb,
    val name: ColorName,
    val source: CaptureSource,
    /** Confidence at the moment of capture; null for photo picks, which are exact pixel values. */
    val confidence: Confidence?,
    val calibrated: Boolean,
    val timeMillis: Long,
)

private fun StoredCapture.toCapture() = Capture(
    id = id,
    rgb = rgb,
    name = ColorNamer.name(rgb),
    source = if (fromPhoto) CaptureSource.PHOTO else CaptureSource.CAMERA,
    confidence = confidence,
    calibrated = calibrated,
    timeMillis = timeMillis,
)

/** Navigation state, saved colors and the reference color for comparing. */
class HuenoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CaptureRepository(application)
    private val prefs = application.getSharedPreferences("hueno", Context.MODE_PRIVATE)

    /** True until the first-run guide has been dismissed once. */
    var showOnboarding by mutableStateOf(!prefs.getBoolean(KEY_ONBOARDING_DONE, false))
        private set

    fun finishOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        showOnboarding = false
    }

    var tab by mutableStateOf(Tab.CAMERA)
        private set

    /** The capture shown on the detail screen, drawn over the current tab; null when closed. */
    var openCapture by mutableStateOf<Capture?>(null)
        private set

    /** Color A when comparing; null when not comparing. Kept here so it survives leaving the camera. */
    var compareReference by mutableStateOf<Rgb?>(null)
        private set

    val captures: StateFlow<List<Capture>> = repository.observeAll()
        .map { rows -> rows.map { it.toCapture() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectTab(target: Tab) {
        openCapture = null
        tab = target
    }

    fun open(capture: Capture) {
        openCapture = capture
    }

    fun closeCapture() {
        openCapture = null
    }

    fun captureFromCamera(reading: ColorReading) = save(reading.rgb, false, reading.confidence, reading.calibrated)

    fun captureFromPhoto(rgb: Rgb) = save(rgb, true, null, false)

    /** Saves the color, then opens it. */
    private fun save(rgb: Rgb, fromPhoto: Boolean, confidence: Confidence?, calibrated: Boolean) {
        viewModelScope.launch {
            openCapture = repository.add(rgb, fromPhoto, confidence, calibrated).toCapture()
        }
    }

    fun delete(capture: Capture) {
        if (openCapture?.id == capture.id) openCapture = null
        viewModelScope.launch { repository.delete(capture.id) }
    }

    fun changeCompareReference(rgb: Rgb?) {
        compareReference = rgb
    }

    /** Uses a saved color as color A and goes to the camera to point at color B. */
    fun compareWithCamera(capture: Capture) {
        compareReference = capture.rgb
        openCapture = null
        tab = Tab.CAMERA
    }

    private companion object {
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
