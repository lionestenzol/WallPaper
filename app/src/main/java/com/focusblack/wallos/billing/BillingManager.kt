package com.focusblack.wallos.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.PurchasesUpdatedListener

class BillingManager(
    private val context: Context,
    private val listener: PurchasesUpdatedListener
) : PurchasesUpdatedListener {

    private val TAG = "BillingManager"
    private var client: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    fun startConnection(onReady: (() -> Unit)? = null) {
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }

            override fun onBillingSetupFinished(billingResult: com.android.billingclient.api.BillingResult) {
                if (billingResult.responseCode == com.android.billingclient.api.BillingClient.BillingResponseCode.OK) {
                    Log.i(TAG, "Billing ready")
                    onReady?.invoke()
                } else {
                    Log.w(TAG, "Billing setup failed: ${billingResult.responseCode}")
                }
            }
        })
    }

    override fun onPurchasesUpdated(
        billingResult: com.android.billingclient.api.BillingResult,
        purchases: MutableList<com.android.billingclient.api.Purchase>?
    ) {
        // Forward to provided listener or handle here
        Log.i(TAG, "onPurchasesUpdated: ${billingResult.responseCode}")
        listener.onPurchasesUpdated(billingResult, purchases)
    }

    fun endConnection() {
        client.endConnection()
    }
}
