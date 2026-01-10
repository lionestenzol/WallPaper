package com.focusblack.wallos.data

import android.content.Context
import androidx.preference.PreferenceManager

class OwnershipStore(private val context: Context) {
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun isOwned(sku: String): Boolean = prefs.getBoolean("own_$sku", false)

    fun setOwned(sku: String, owned: Boolean) {
        prefs.edit().putBoolean("own_$sku", owned).apply()
    }
}
