package com.hueno.monetization

import android.app.Activity
import android.content.Context
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Identifiers that differ per app. The ad app id itself lives in the manifest. */
data class MonetizationConfig(
    val proProductId: String,
    val bannerUnitId: String,
    val rewardedUnitId: String,
)

enum class RewardedStatus { UNAVAILABLE, LOADING, READY }

data class MonetizationState(
    /** Pro was bought. */
    val purchased: Boolean = false,
    /** Milliseconds left on a rewarded pass; 0 when none is running. */
    val passRemainingMillis: Long = 0L,
    /** Localized price of Pro from Google Play; null until Play has answered. */
    val price: String? = null,
    /** The ad SDK is running and consent allows requesting ads. */
    val adsReady: Boolean = false,
    val rewarded: RewardedStatus = RewardedStatus.UNAVAILABLE,
    /** Another video would add no time because the pass is already at the cap. */
    val passAtCap: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val comparesLeftToday: Int = ProRules.FREE_COMPARES_PER_DAY,
) {
    /** The single source of truth for "is this user on Pro right now". */
    val isPro: Boolean get() = purchased || passRemainingMillis > 0

    /** Banners are shown only to free users, and only once ads have been set up. */
    val showBanners: Boolean get() = adsReady && !isPro
}

/**
 * Everything about Pro, the rewarded pass, free limits and ads behind one state flow.
 *
 * Rules the rest of the app can rely on:
 * - Nothing ad-related starts until [prepareAds] is called.
 * - Rewarded videos play only from [watchRewarded].
 * - Any failure in billing or ads leaves the app usable.
 */
class Monetization(context: Context, val config: MonetizationConfig) {

    private val store = MonetizationStore(context)
    private val _state = MutableStateFlow(MonetizationState())
    val state: StateFlow<MonetizationState> = _state.asStateFlow()

    private val _billingEvents = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 8)

    /** One-off outcomes of a purchase or restore the user started. Not replayed to late collectors. */
    val billingEvents: SharedFlow<BillingEvent> = _billingEvents.asSharedFlow()

    private val billing = ProBilling(
        context = context,
        productId = config.proProductId,
        onPurchased = { owned ->
            store.purchased = owned
            refresh()
        },
        onPrice = { price -> setPrice(price) },
        onEvent = { event -> _billingEvents.tryEmit(event) },
    )

    private val ads = AdsController(context, config.rewardedUnitId) { refresh() }

    /** Number of this app start, counted from 1. Ads are held back during the first one. */
    val session: Int

    init {
        session = store.sessions + 1
        store.sessions = session
        refresh()
        billing.start()
    }

    /**
     * Recomputes the state from the clock and stored values. Cheap; call it on a timer.
     * Synchronized because billing and ad callbacks arrive on other threads.
     */
    @Synchronized
    fun refresh() {
        val now = System.currentTimeMillis()
        val observed = ProPass.observe(store.pass, now)
        if (observed != store.pass) store.pass = observed
        val previous = _state.value
        _state.value = previous.copy(
            purchased = store.purchased,
            passRemainingMillis = ProPass.remainingMillis(observed, now),
            passAtCap = !ProPass.canGrantMore(observed, now),
            adsReady = ads.ready,
            rewarded = when {
                ads.rewardedLoaded -> RewardedStatus.READY
                ads.rewardedLoading -> RewardedStatus.LOADING
                else -> RewardedStatus.UNAVAILABLE
            },
            privacyOptionsRequired = ads.privacyOptionsRequired,
            comparesLeftToday = store.compareQuota.remaining(today()),
        )
    }

    @Synchronized
    private fun setPrice(price: String?) {
        _state.value = _state.value.copy(price = price)
    }

    /** Gathers consent where required and starts ads. Call from the second session on. */
    fun prepareAds(activity: Activity) {
        ads.prepare(activity) { allowed -> if (allowed) ads.loadRewarded() }
    }

    /** Starts a comparison if the user may. Free users get a daily allowance. */
    fun tryStartCompare(): Boolean {
        if (_state.value.isPro) return true
        val today = today()
        val quota = store.compareQuota
        if (quota.remaining(today) <= 0) return false
        store.compareQuota = quota.use(today)
        refresh()
        return true
    }

    /** Whether one more color may be saved, given how many are saved already. */
    fun canSave(savedCount: Int): Boolean = _state.value.isPro || savedCount < ProRules.FREE_HISTORY_LIMIT

    /** Whether one more calibration may be saved, given how many are saved already. */
    fun canSaveProfile(savedCount: Int): Boolean = _state.value.isPro || savedCount < ProRules.FREE_PROFILE_LIMIT

    /** Opens the Google Play purchase sheet. Returns false when the product is not available. */
    fun buyPro(activity: Activity): Boolean = billing.buy(activity)

    /** Asks Play again what this account owns; the outcome arrives on [billingEvents]. */
    fun restorePurchases() = billing.refreshPurchases(userAsked = true)

    /**
     * Plays a rewarded video and grants the pass if it is watched to the end. Sets up consent
     * and ads first if that has not happened yet, because the user asked for the video.
     * [onResult] reports whether Pro time was granted.
     */
    fun watchRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        val grant = {
            store.pass = ProPass.grant(store.pass, System.currentTimeMillis())
            refresh()
        }
        val closed = { earned: Boolean ->
            ads.loadRewarded()
            onResult(earned)
        }
        if (ads.showRewarded(activity, grant, closed)) return

        // Nothing loaded yet: prepare, load one, then play it.
        ads.prepare(activity) { allowed ->
            if (!allowed) {
                onResult(false)
            } else {
                awaitSdkThenLoad(attemptsLeft = SDK_WAIT_ATTEMPTS) { loaded ->
                    if (!loaded || !ads.showRewarded(activity, grant, closed)) onResult(false)
                }
            }
        }
    }

    /** The SDK finishes starting on a background thread; poll briefly before loading a video. */
    private fun awaitSdkThenLoad(attemptsLeft: Int, onLoaded: (Boolean) -> Unit) {
        when {
            ads.ready -> ads.loadRewarded(onLoaded)
            attemptsLeft <= 0 -> onLoaded(false)
            else -> mainHandler.postDelayed({ awaitSdkThenLoad(attemptsLeft - 1, onLoaded) }, 250)
        }
    }

    fun showPrivacyOptions(activity: Activity) = ads.showPrivacyOptions(activity)

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private companion object {
        /** Up to 20 seconds for the ad SDK's first start, which is slow on a cold device. */
        const val SDK_WAIT_ATTEMPTS = 80
    }

    private fun today(): Long = LocalDate.now().toEpochDay()
}
