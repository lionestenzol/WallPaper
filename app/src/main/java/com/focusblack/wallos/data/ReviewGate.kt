package com.focusblack.wallos.data

import android.content.Context
import androidx.preference.PreferenceManager

class ReviewGate(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val KEY_APPLIES = "review_applies_count"
    private val KEY_REVIEWED = "review_shown"

    fun recordApply() {
        val count = prefs.getInt(KEY_APPLIES, 0) + 1
        prefs.edit().putInt(KEY_APPLIES, count).apply()
    }

    fun shouldShowReview(): Boolean {
        val count = prefs.getInt(KEY_APPLIES, 0)
        val shown = prefs.getBoolean(KEY_REVIEWED, false)
        return !shown && count >= 3
    }

    fun setShown() {
        prefs.edit().putBoolean(KEY_REVIEWED, true).apply()
    }
}
