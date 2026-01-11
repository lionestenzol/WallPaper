package com.focusblack.wallos.billing

import android.app.Activity
import android.app.Application
import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Simple wrapper around BillingManager for use in UI/ViewModels.
 * Provides a cleaner API for common billing operations.
 */
class BillingRepository private constructor(
    application: Application
) {
    private val billingManager = BillingManager(application)

    private var isInitialized = false

    companion object {
        private const val TAG = "BillingRepository"

        @Volatile
        private var instance: BillingRepository? = null

        fun getInstance(application: Application): BillingRepository {
            return instance ?: synchronized(this) {
                instance ?: BillingRepository(application).also { instance = it }
            }
        }
    }

    // Initialize billing connection
    suspend fun initialize(): Boolean = suspendCancellableCoroutine { continuation ->
        if (isInitialized) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        billingManager.startConnection(
            onReady = {
                isInitialized = true
                continuation.resume(true)
            },
            onError = {
                continuation.resume(false)
            }
        )
    }

    // Load product details and return price
    suspend fun loadProPrice(): String? {
        if (!isInitialized) {
            initialize()
        }
        billingManager.queryProductDetails()
        return billingManager.getProPrice()
    }

    // Check if user is Pro (local check)
    fun isPro(): Boolean = billingManager.isPro()

    // Launch purchase flow
    fun purchasePro(activity: Activity, onComplete: (success: Boolean) -> Unit) {
        billingManager.onPurchaseComplete = { success ->
            onComplete(success)
        }

        val launched = billingManager.launchPurchaseFlow(activity)
        if (!launched) {
            Log.w(TAG, "Failed to launch purchase flow")
            onComplete(false)
        }
    }

    // Restore purchases
    suspend fun restorePurchases(): Boolean {
        if (!isInitialized) {
            initialize()
        }
        return billingManager.queryPurchases()
    }

    // Get cached price (call loadProPrice first)
    fun getCachedProPrice(): String? = billingManager.getProPrice()

    // Cleanup
    fun destroy() {
        billingManager.endConnection()
        instance = null
    }
}
