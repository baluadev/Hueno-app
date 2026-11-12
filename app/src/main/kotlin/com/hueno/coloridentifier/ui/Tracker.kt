package com.hueno.coloridentifier.ui

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.hueno.monetization.AdRevenue

/**
 * Usage counts sent to Firebase Analytics.
 *
 * Only what is needed to see how the app is used: which screens are opened, how colors are saved,
 * where free limits are hit and how Pro is reached. Never the measured color values, names the
 * user typed, collection names or pictures. The color family ("Blue") is sent, not the hex code.
 * The advertising ID is not collected (see the manifest), and nothing here is used for ads.
 */
class Tracker(context: Context) {

    private val analytics = FirebaseAnalytics.getInstance(context.applicationContext)

    fun screen(name: String) = log(FirebaseAnalytics.Event.SCREEN_VIEW) {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        putString(FirebaseAnalytics.Param.SCREEN_CLASS, name)
    }

    fun onboardingDone() = log(FirebaseAnalytics.Event.TUTORIAL_COMPLETE)

    fun colorSaved(fromPhoto: Boolean, family: String, calibrated: Boolean, confidence: String?) =
        log("color_saved") {
            putString("source", if (fromPhoto) "photo" else "camera")
            putString("family", family)
            putString("calibrated", calibrated.toString())
            confidence?.let { putString("confidence", it) }
        }

    fun compareStarted(source: String) = log("compare_start") { putString("source", source) }

    fun limitReached(reason: String) = log("limit_reached") { putString("reason", reason) }

    fun proScreen(reason: String) = log("pro_screen") { putString("reason", reason) }

    fun rewarded(granted: Boolean) = log("rewarded_video") { putString("granted", granted.toString()) }

    fun billing(result: String) = log("pro_billing") { putString("result", result) }

    fun export(ok: Boolean) = log("export_csv") { putString("ok", ok.toString()) }

    /**
     * One paid ad impression, as `ad_impression_scaled`. The value is multiplied by 100 so that
     * fractions of a cent survive as a usable number in Analytics and in the exported data,
     * impressions worth less than 0.0001 are dropped, and one impression counts at most 25
     * (0.25 in real money) so a single outlier cannot distort the totals. Matches the event
     * the studio's other apps send, so dashboards can be shared.
     */
    fun adPaid(revenue: AdRevenue) {
        val scaled = revenue.scaledValue() ?: return
        log("ad_impression_scaled") {
            putString("ad_platform", "AdMob")
            putString("ad_source", revenue.source)
            putString("ad_unit_name", revenue.adUnitId)
            putString("ad_format", revenue.format)
            putDouble(FirebaseAnalytics.Param.VALUE, scaled)
            putString(FirebaseAnalytics.Param.CURRENCY, revenue.currency)
            putString("precision", revenue.precision)
        }
    }

    private inline fun log(event: String, params: Bundle.() -> Unit = {}) {
        analytics.logEvent(event, Bundle().apply(params))
    }
}
