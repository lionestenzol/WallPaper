package com.focusblack.wallos.data

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class OwnershipStore(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val KEY_PRO = "own_pro_unlock"
    }

    fun isPro(): Boolean = prefs.getBoolean(KEY_PRO, false)

    fun setPro(owned: Boolean) {
        prefs.edit { putBoolean(KEY_PRO, owned) }
    }
}
