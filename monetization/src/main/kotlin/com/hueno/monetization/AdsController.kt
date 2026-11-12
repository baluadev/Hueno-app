package com.hueno.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Consent, ad SDK start-up and the rewarded video.
 *
 * Nothing here starts on its own: [prepare] is called by the app when ads are due, and the
 * rewarded video only ever plays from [showRewarded], which the user triggers. Every failure
 * leaves the app working without ads.
 */
internal class AdsController(
    context: Context,
    private val rewardedUnitId: String,
    private val onChanged: () -> Unit,
    /** Called on the main thread for every paid rewarded impression. */
    private val onPaid: (AdRevenue) -> Unit = {},
) {
    private val appContext = context.applicationContext
    private val consent: ConsentInformation = UserMessagingPlatform.getConsentInformation(appContext)
    private val initialized = AtomicBoolean(false)

    /** True once the SDK is running and ads may be requested. */
    @Volatile
    var ready = false
        private set

    @Volatile
    var rewardedLoading = false
        private set

    private var rewarded: RewardedAd? = null

    val rewardedLoaded: Boolean get() = rewarded != null

    /** True where the law requires a way to revisit the consent choice. */
    val privacyOptionsRequired: Boolean
        get() = consent.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Gathers consent where required, then starts the ad SDK. Safe to call more than once.
     * [done] runs on the main thread with whether ads may be requested.
     */
    fun prepare(activity: Activity, done: (Boolean) -> Unit = {}) {
        // A choice made in an earlier session is still valid, so ads can start right away.
        if (consent.canRequestAds()) startSdk()

        consent.requestConsentInfoUpdate(
            activity,
            ConsentRequestParameters.Builder().build(),
            {
                // Shows the form only where a choice is required and not yet made. Declining
                // personalized ads still ends here, with ads allowed in non-personalized form.
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { error ->
                    if (error != null) Log.w(TAG, "Consent form: ${error.message}")
                    if (consent.canRequestAds()) startSdk()
                    onChanged()
                    done(consent.canRequestAds())
                }
            },
            { error ->
                Log.w(TAG, "Consent update failed: ${error.message}")
                onChanged()
                done(consent.canRequestAds())
            },
        )
    }

    private fun startSdk() {
        if (!initialized.compareAndSet(false, true)) return
        // Initialization does disk and network work; keep it off the main thread.
        Thread {
            MobileAds.initialize(appContext) {
                ready = true
                onChanged()
            }
        }.start()
    }

    /** Loads a rewarded video ahead of time. Call on the main thread. */
    fun loadRewarded(onResult: (Boolean) -> Unit = {}) {
        if (!ready || rewardedLoading || rewarded != null) {
            onResult(rewarded != null)
            return
        }
        rewardedLoading = true
        onChanged()
        RewardedAd.load(
            appContext,
            rewardedUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.setOnPaidEventListener { value ->
                        onPaid(AdRevenue.from("rewarded", rewardedUnitId, value, ad.responseInfo))
                    }
                    rewarded = ad
                    rewardedLoading = false
                    onChanged()
                    onResult(true)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "Rewarded failed to load: ${error.message}")
                    rewarded = null
                    rewardedLoading = false
                    onChanged()
                    onResult(false)
                }
            },
        )
    }

    /**
     * Plays the loaded rewarded video. [onReward] runs if the user earned the reward;
     * [onClosed] always runs afterwards with whether it was earned. Returns false if no video
     * is loaded.
     */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onClosed: (Boolean) -> Unit): Boolean {
        val ad = rewarded ?: return false
        rewarded = null
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onChanged()
                onClosed(earned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "Rewarded failed to show: ${error.message}")
                onChanged()
                onClosed(false)
            }
        }
        ad.show(activity) {
            earned = true
            onReward()
        }
        onChanged()
        return true
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            if (error != null) Log.w(TAG, "Privacy options: ${error.message}")
            onChanged()
        }
    }

    private companion object {
        const val TAG = "AdsController"
    }
}
