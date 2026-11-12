package com.hueno.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * The one-time Pro purchase through Google Play Billing.
 *
 * Callbacks may arrive on any thread. [onPurchased] is only called when Play has given a definite
 * answer, so a failed or offline query never revokes Pro.
 */
internal class ProBilling(
    context: Context,
    private val productId: String,
    private val onPurchased: (Boolean) -> Unit,
    private val onPrice: (String?) -> Unit,
) {
    @Volatile
    private var details: ProductDetails? = null

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                handle(purchases, definitive = false)
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct()
                    refreshPurchases()
                } else {
                    Log.w(TAG, "Billing setup failed: ${result.responseCode} ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // Automatic reconnection is enabled; nothing to do here.
            }
        })
    }

    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
        client.queryProductDetailsAsync(params) { result, response ->
            val found = if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                response.productDetailsList.firstOrNull { it.productId == productId }
            } else {
                null
            }
            details = found
            onPrice(found?.oneTimePurchaseOfferDetails?.formattedPrice)
        }
    }

    /** Asks Play what this account owns. Also serves as "Restore purchase". */
    fun refreshPurchases() {
        if (!client.isReady) return
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) handle(purchases, definitive = true)
        }
    }

    /**
     * [definitive] is true for a full list of owned purchases, where the absence of Pro means it
     * is not owned. A purchase update only lists what just changed, so it can grant but not revoke.
     */
    private fun handle(purchases: List<Purchase>, definitive: Boolean) {
        val owned = purchases.filter {
            productId in it.products && it.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        owned.filterNot { it.isAcknowledged }.forEach { purchase ->
            // Play refunds purchases that are not acknowledged within three days.
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
            client.acknowledgePurchase(params) { result ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    Log.w(TAG, "Acknowledge failed: ${result.responseCode}")
                }
            }
        }
        if (owned.isNotEmpty()) onPurchased(true) else if (definitive) onPurchased(false)
    }

    /** Opens the Play purchase sheet. Returns false when the product is not available yet. */
    fun buy(activity: Activity): Boolean {
        val product = details ?: return false
        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product)
        product.oneTimePurchaseOfferDetails?.offerToken?.takeIf { it.isNotEmpty() }?.let(productParams::setOfferToken)
        val params = BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(productParams.build())).build()
        return client.launchBillingFlow(activity, params).responseCode == BillingClient.BillingResponseCode.OK
    }

    private companion object {
        const val TAG = "ProBilling"
    }
}
