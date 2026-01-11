package com.focusblack.wallos.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.Purchase
import com.focusblack.wallos.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class PurchaseVerifier(private val context: Context) {

    suspend fun verifyPurchase(purchase: Purchase): VerificationResult = withContext(Dispatchers.IO) {
        val productId = purchase.products.firstOrNull().orEmpty()
        if (productId.isBlank()) {
            return@withContext VerificationResult.Unverified("missing_product_id")
        }

        val payload = JSONObject()
            .put("packageName", context.packageName)
            .put("productId", productId)
            .put("purchaseToken", purchase.purchaseToken)
            .put("purchaseTimeMillis", purchase.purchaseTime)

        try {
            val connection = URL(BuildConfig.PURCHASE_VERIFY_URL).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(payload.toString())
            }

            val responseCode = connection.responseCode
            val responseBody = (if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            })?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (responseCode in 200..299) {
                val json = runCatching { JSONObject(responseBody) }.getOrNull()
                val verified = json?.optBoolean("verified", false) ?: false
                val verifiedAt = json?.optLong("verifiedAtMillis", 0L)?.takeIf { it > 0 }
                return@withContext if (verified) {
                    VerificationResult.Verified(verifiedAt)
                } else {
                    VerificationResult.Unverified("backend_declined")
                }
            }

            if (responseCode in 400..499) {
                return@withContext VerificationResult.Unverified("client_error_$responseCode")
            }

            return@withContext VerificationResult.Error("server_error_$responseCode")
        } catch (exception: IOException) {
            Log.w(TAG, "Verification failed: ${exception.message}")
            return@withContext VerificationResult.Error("network_error")
        }
    }

    sealed class VerificationResult {
        data class Verified(val verifiedAtMillis: Long?) : VerificationResult()
        data class Unverified(val reason: String) : VerificationResult()
        data class Error(val reason: String) : VerificationResult()
    }

    companion object {
        private const val TAG = "PurchaseVerifier"
        private const val TIMEOUT_MS = 12_000
    }
}
