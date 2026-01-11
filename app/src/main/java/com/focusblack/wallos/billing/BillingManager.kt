package com.focusblack.wallos.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.focusblack.wallos.data.OwnershipStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillingManager(
    private val context: Context
) : PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    private var productDetails: ProductDetails? = null
    private val ownershipStore = OwnershipStore(context)

    var onPurchaseComplete: ((success: Boolean) -> Unit)? = null

    // Connect to billing service
    fun startConnection(onReady: (() -> Unit)? = null, onError: (() -> Unit)? = null) {
        if (billingClient.isReady) {
            onReady?.invoke()
            return
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing ready")
                    onReady?.invoke()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.responseCode}")
                    onError?.invoke()
                }
            }
        })
    }

    // Query product details (price info)
    suspend fun queryProductDetails(): ProductDetails? = withContext(Dispatchers.IO) {
        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready")
            return@withContext null
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SkuCatalog.SKU_PRO)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        val result = billingClient.queryProductDetails(params)

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            productDetails = result.productDetailsList?.firstOrNull()
            Log.i(TAG, "Product details loaded: ${productDetails?.name}")
            return@withContext productDetails
        } else {
            Log.w(TAG, "Failed to query product details: ${result.billingResult.responseCode}")
            return@withContext null
        }
    }

    // Get formatted price string
    fun getProPrice(): String? {
        return productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
    }

    // Launch purchase flow
    fun launchPurchaseFlow(activity: Activity): Boolean {
        val details = productDetails
        if (details == null) {
            Log.w(TAG, "Product details not loaded")
            return false
        }

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    // Handle purchase updates (called by Play Billing)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    handlePurchase(purchase)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.i(TAG, "Purchase canceled by user")
                onPurchaseComplete?.invoke(false)
            }
            else -> {
                Log.w(TAG, "Purchase failed: ${billingResult.responseCode}")
                onPurchaseComplete?.invoke(false)
            }
        }
    }

    // Process a purchase
    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement
            if (purchase.products.contains(SkuCatalog.SKU_PRO)) {
                ownershipStore.setPro(true)
                Log.i(TAG, "Pro unlocked!")
            }

            // Acknowledge the purchase
            if (!purchase.isAcknowledged) {
                acknowledgePurchase(purchase)
            }

            onPurchaseComplete?.invoke(true)
        } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            Log.i(TAG, "Purchase pending...")
        }
    }

    // Acknowledge purchase (required within 3 days)
    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.i(TAG, "Purchase acknowledged")
            } else {
                Log.w(TAG, "Failed to acknowledge: ${result.responseCode}")
            }
        }
    }

    // Query existing purchases (for restore)
    suspend fun queryPurchases(): Boolean = withContext(Dispatchers.IO) {
        if (!billingClient.isReady) {
            Log.w(TAG, "Billing client not ready")
            return@withContext false
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val result = billingClient.queryPurchasesAsync(params)

        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            var foundPro = false
            result.purchasesList.forEach { purchase ->
                if (purchase.products.contains(SkuCatalog.SKU_PRO) &&
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    ownershipStore.setPro(true)
                    foundPro = true
                    Log.i(TAG, "Restored Pro purchase")
                }
            }
            return@withContext foundPro
        } else {
            Log.w(TAG, "Failed to query purchases: ${result.billingResult.responseCode}")
            return@withContext false
        }
    }

    // Check if pro is owned locally
    fun isPro(): Boolean = ownershipStore.isPro()

    // Disconnect
    fun endConnection() {
        billingClient.endConnection()
    }

    companion object {
        private const val TAG = "BillingManager"
    }
}
