package com.focusblack.wallos.data

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class OwnershipStore(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val KEY_PRO = "own_pro_unlock"
        private const val KEY_PRO_VERIFIED_AT = "own_pro_verified_at"
        private const val PRO_GRACE_PERIOD_MS = 3 * 24 * 60 * 60 * 1000L
    }

    fun isPro(): Boolean = isProCacheValid()

    fun isProCacheValid(now: Long = System.currentTimeMillis()): Boolean {
        val owned = prefs.getBoolean(KEY_PRO, false)
        if (!owned) {
            return false
        }
        val verifiedAt = prefs.getLong(KEY_PRO_VERIFIED_AT, 0L)
        if (verifiedAt <= 0L) {
            return false
        }
        return now - verifiedAt <= PRO_GRACE_PERIOD_MS
    }

    fun setProVerified(owned: Boolean, verifiedAtMillis: Long = System.currentTimeMillis()) {
        prefs.edit {
            putBoolean(KEY_PRO, owned)
            putLong(KEY_PRO_VERIFIED_AT, if (owned) verifiedAtMillis else 0L)
        }
    }
}
