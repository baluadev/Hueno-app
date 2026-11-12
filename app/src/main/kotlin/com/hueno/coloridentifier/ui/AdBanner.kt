package com.hueno.coloridentifier.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hueno.monetization.AdRevenue

/** Where banners report paid impressions; provided once at the top of the app. */
val LocalAdPaid = staticCompositionLocalOf<(AdRevenue) -> Unit> { {} }

/**
 * A banner in a slot of fixed height. The height is reserved before the ad arrives and stays
 * empty if none does, so the layout never jumps and the app never waits on an ad.
 */
@Composable
fun AdBanner(unitId: String, modifier: Modifier = Modifier, onPaid: (AdRevenue) -> Unit = LocalAdPaid.current) {
    val context = LocalContext.current
    BoxWithConstraints(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        val widthDp = maxWidth.value.toInt()
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        Box(Modifier.fillMaxWidth().height(adSize.height.dp), contentAlignment = Alignment.Center) {
            AndroidView(
                factory = { viewContext ->
                    AdView(viewContext).apply {
                        setAdSize(adSize)
                        adUnitId = unitId
                        setOnPaidEventListener { value -> onPaid(AdRevenue.from("banner", unitId, value, responseInfo)) }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
