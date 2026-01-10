package com.focusblack.wallos.data

import android.content.Context
import androidx.preference.PreferenceManager
import com.focusblack.wallos.model.UserState

class UserRepository(private val context: Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun getUserState(): UserState {
        val pro = prefs.getBoolean("owned_pro", false)
        return UserState(isPro = pro)
    }

    fun setProOwned(owned: Boolean) {
        prefs.edit().putBoolean("owned_pro", owned).apply()
    }
}
