package com.focusblack.wallos.data

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class ReviewGate(context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val KEY_APPLIES = "review_applies_count"
        private const val KEY_REVIEWED = "review_shown"
    }

    fun recordApply() {
        val count = prefs.getInt(KEY_APPLIES, 0) + 1
        prefs.edit { putInt(KEY_APPLIES, count) }
    }

    fun shouldShowReview(): Boolean {
        val count = prefs.getInt(KEY_APPLIES, 0)
        val shown = prefs.getBoolean(KEY_REVIEWED, false)
        return !shown && count >= 3
    }

    fun setShown() {
        prefs.edit { putBoolean(KEY_REVIEWED, true) }
    }
}
