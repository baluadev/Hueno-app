package com.hueno.coloridentifier.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hueno.coloridentifier.BuildConfig
import com.hueno.core.color.ColorName
import com.hueno.core.color.ColorNamer
import com.hueno.core.color.ColorReading
import com.hueno.core.color.Confidence
import com.hueno.core.color.Rgb
import com.hueno.data.CaptureRepository
import com.hueno.data.StoredCapture
import com.hueno.monetization.Monetization
import com.hueno.monetization.MonetizationConfig
import com.hueno.monetization.MonetizationState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class Tab { CAMERA, PHOTO, HISTORY }

enum class CaptureSource { CAMERA, PHOTO }

/** Why the Pro screen is open. The limit reasons add a line explaining what was reached. */
enum class ProReason { MENU, COMPARE_LIMIT, HISTORY_LIMIT }

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

/** Navigation state, saved colors, the comparison reference, and Pro and ads. */
class HuenoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CaptureRepository(application)
    private val prefs = application.getSharedPreferences("hueno", Context.MODE_PRIVATE)

    private val monetization = Monetization(
        application,
        MonetizationConfig(
            proProductId = BuildConfig.PRO_PRODUCT_ID,
            bannerUnitId = BuildConfig.AD_BANNER_UNIT_ID,
            rewardedUnitId = BuildConfig.AD_REWARDED_UNIT_ID,
        ),
    )

    /** Pro, pass, price and ad state. */
    val money: StateFlow<MonetizationState> = monetization.state

    val bannerUnitId: String get() = monetization.config.bannerUnitId

    /** True until the first-run guide has been dismissed once. */
    var showOnboarding by mutableStateOf(!prefs.getBoolean(KEY_ONBOARDING_DONE, false))
        private set

    var tab by mutableStateOf(Tab.CAMERA)
        private set

    /** The capture shown on the detail screen, drawn over the current tab; null when closed. */
    var openCapture by mutableStateOf<Capture?>(null)
        private set

    /** Color A when comparing; null when not comparing. Kept here so it survives leaving the camera. */
    var compareReference by mutableStateOf<Rgb?>(null)
        private set

    /** Non-null while the Pro screen is open. */
    var proScreen by mutableStateOf<ProReason?>(null)
        private set

    /** True while a rewarded video is being fetched or played. */
    var rewardedBusy by mutableStateOf(false)
        private set

    // Collected eagerly: the free limit needs the saved count even when History is not on screen.
    val captures: StateFlow<List<Capture>> = repository.observeAll()
        .map { rows -> rows.map { it.toCapture() } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        // Keeps the pass countdown current and ends Pro when the pass runs out.
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                monetization.refresh()
            }
        }
    }

    fun finishOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, true).apply()
        showOnboarding = false
    }

    /**
     * Called once the main screen is up. The first session has no ads and no consent dialog;
     * from the second one, free users get consent (where required) and ads.
     */
    fun onMainScreenShown(activity: Activity) {
        if (monetization.session >= 2 && !money.value.isPro) monetization.prepareAds(activity)
    }

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

    /** Saves the color and opens it, or opens the Pro screen when the free limit is reached. */
    private fun save(rgb: Rgb, fromPhoto: Boolean, confidence: Confidence?, calibrated: Boolean) {
        if (!monetization.canSave(captures.value.size)) {
            proScreen = ProReason.HISTORY_LIMIT
            return
        }
        viewModelScope.launch {
            openCapture = repository.add(rgb, fromPhoto, confidence, calibrated).toCapture()
        }
    }

    fun delete(capture: Capture) {
        if (openCapture?.id == capture.id) openCapture = null
        viewModelScope.launch { repository.delete(capture.id) }
    }

    /** Starts a comparison against [rgb], or ends it when null. Free users have a daily allowance. */
    fun changeCompareReference(rgb: Rgb?) {
        if (rgb == null) {
            compareReference = null
        } else if (monetization.tryStartCompare()) {
            compareReference = rgb
        } else {
            proScreen = ProReason.COMPARE_LIMIT
        }
    }

    /** Uses a saved color as color A and goes to the camera to point at color B. */
    fun compareWithCamera(capture: Capture) {
        if (!monetization.tryStartCompare()) {
            proScreen = ProReason.COMPARE_LIMIT
            return
        }
        compareReference = capture.rgb
        openCapture = null
        tab = Tab.CAMERA
    }

    fun openPro() {
        proScreen = ProReason.MENU
    }

    fun closePro() {
        proScreen = null
    }

    /** Opens the Google Play purchase sheet. Returns false when Pro cannot be bought right now. */
    fun buyPro(activity: Activity): Boolean = monetization.buyPro(activity)

    fun restorePurchases() = monetization.restorePurchases()

    /** Plays a rewarded video the user asked for. [onResult] reports whether Pro time was granted. */
    fun watchRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        if (rewardedBusy) return
        rewardedBusy = true
        monetization.watchRewarded(activity) { granted ->
            rewardedBusy = false
            onResult(granted)
        }
    }

    fun showPrivacyOptions(activity: Activity) = monetization.showPrivacyOptions(activity)

    private companion object {
        const val KEY_ONBOARDING_DONE = "onboarding_done"
    }
}
