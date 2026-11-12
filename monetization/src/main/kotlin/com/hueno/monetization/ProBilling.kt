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

/** Outcomes of a purchase or restore the user started, each one worth telling them about. */
enum class BillingEvent {
    PURCHASED,
    /** Play accepted the payment but has not completed it, for example a cash payment. */
    PENDING,
    /** The purchase sheet reported an error other than the user closing it. */
    PURCHASE_FAILED,
    RESTORED,
    NOTHING_TO_RESTORE,
    /** Google Play could not be reached to check purchases. */
    RESTORE_FAILED,
}

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
    private val onEvent: (BillingEvent) -> Unit,
) {
    @Volatile
    private var details: ProductDetails? = null

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener { result, purchases ->
            when (result.responseCode) {
                BillingClient.BillingResponseCode.OK -> if (purchases != null) {
                    when (handle(purchases, definitive = false)) {
                        Ownership.OWNED -> onEvent(BillingEvent.PURCHASED)
                        Ownership.PENDING -> onEvent(BillingEvent.PENDING)
                        Ownership.NONE -> Unit
                    }
                }
                // Closing the sheet is a choice, not an error.
                BillingClient.BillingResponseCode.USER_CANCELED -> Unit
                // Bought before, on this account: the same as a restore.
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refreshPurchases(userAsked = true)
                else -> {
                    Log.w(TAG, "Purchase failed: ${result.responseCode} ${result.debugMessage}")
                    onEvent(BillingEvent.PURCHASE_FAILED)
                }
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

    /**
     * Asks Play what this account owns. Also serves as "Restore purchase": with [userAsked] every
     * outcome, including a failure to reach Play, is reported through onEvent.
     */
    fun refreshPurchases(userAsked: Boolean = false) {
        if (!client.isReady) {
            if (userAsked) onEvent(BillingEvent.RESTORE_FAILED)
            return
        }
        val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                val ownership = handle(purchases, definitive = true)
                if (userAsked) {
                    onEvent(
                        when (ownership) {
                            Ownership.OWNED -> BillingEvent.RESTORED
                            Ownership.PENDING -> BillingEvent.PENDING
                            Ownership.NONE -> BillingEvent.NOTHING_TO_RESTORE
                        },
                    )
                }
            } else if (userAsked) {
                Log.w(TAG, "Restore failed: ${result.responseCode} ${result.debugMessage}")
                onEvent(BillingEvent.RESTORE_FAILED)
            }
        }
    }

    private enum class Ownership { OWNED, PENDING, NONE }

    /**
     * [definitive] is true for a full list of owned purchases, where the absence of Pro means it
     * is not owned. A purchase update only lists what just changed, so it can grant but not revoke.
     */
    private fun handle(purchases: List<Purchase>, definitive: Boolean): Ownership {
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
        val pending = purchases.any {
            productId in it.products && it.purchaseState == Purchase.PurchaseState.PENDING
        }
        return when {
            owned.isNotEmpty() -> Ownership.OWNED
            pending -> Ownership.PENDING
            else -> Ownership.NONE
        }
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
