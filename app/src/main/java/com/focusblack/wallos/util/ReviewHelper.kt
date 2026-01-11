package com.focusblack.wallos.util

import android.app.Activity
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

object ReviewHelper {
    private const val TAG = "ReviewHelper"

    fun showReviewIfAppropriate(activity: Activity) {
        val reviewManager = ReviewManagerFactory.create(activity)

        // Request the ReviewInfo object
        val request = reviewManager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = task.result

                // Launch the in-app review flow
                val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                flow.addOnCompleteListener {
                    // The review flow has finished
                    // Note: This does not indicate whether the user reviewed or not,
                    // or even whether the review dialog was shown
                    Log.i(TAG, "Review flow completed")
                }
            } else {
                // There was some problem, log it
                Log.w(TAG, "Failed to request review flow", task.exception)
            }
        }
    }
}
