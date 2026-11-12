package com.hueno.monetization

import com.google.android.gms.ads.AdValue
import com.google.android.gms.ads.ResponseInfo

/**
 * One paid ad impression, as reported by AdMob's OnPaidEventListener.
 *
 * [value] is in units of [currency], converted from AdMob's micros. The currency is the one set in
 * the AdMob account, which is not always USD.
 */
data class AdRevenue(
    val format: String,
    val adUnitId: String,
    /** The network that served the ad, for example "AdMob Network"; "unknown" when not reported. */
    val source: String,
    val value: Double,
    val currency: String,
    /** "estimated", "publisher_provided", "precise" or "unknown". */
    val precision: String,
) {
    /**
     * The value reported as `ad_impression_scaled`: multiplied by 100, null below 0.01 (the
     * impression is not reported), and capped at 25 so one outlier cannot distort the totals.
     */
    fun scaledValue(): Double? {
        val scaled = value * SCALE
        if (scaled.isNaN() || scaled < MIN_SCALED) return null
        return minOf(scaled, MAX_SCALED)
    }

    companion object {
        const val SCALE = 100.0
        const val MIN_SCALED = 0.01
        const val MAX_SCALED = 25.0

        fun from(format: String, adUnitId: String, adValue: AdValue, responseInfo: ResponseInfo?): AdRevenue =
            AdRevenue(
                format = format,
                adUnitId = adUnitId,
                source = responseInfo?.loadedAdapterResponseInfo?.adSourceName?.takeIf { it.isNotBlank() } ?: "unknown",
                value = adValue.valueMicros / 1_000_000.0,
                currency = adValue.currencyCode.ifBlank { "USD" },
                precision = when (adValue.precisionType) {
                    AdValue.PrecisionType.ESTIMATED -> "estimated"
                    AdValue.PrecisionType.PUBLISHER_PROVIDED -> "publisher_provided"
                    AdValue.PrecisionType.PRECISE -> "precise"
                    else -> "unknown"
                },
            )
    }
}
