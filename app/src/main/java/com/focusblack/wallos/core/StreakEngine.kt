package com.focusblack.wallos.core

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StreakEngine {
    private const val KEY_LAST_APPLY = "streak_last_apply"
    private const val KEY_STREAK = "streak_count"

    fun onDailyApplied(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val today = LocalDate.now(ZoneId.systemDefault())
        val last = prefs.getString(KEY_LAST_APPLY, null)
        val lastDate = last?.let { LocalDate.parse(it) }
        val streak = prefs.getInt(KEY_STREAK, 0)
        val newStreak = when {
            lastDate == null -> 1
            lastDate.plusDays(1).isEqual(today) -> streak + 1
            lastDate.isEqual(today) -> streak
            else -> 1
        }
        prefs.edit()
            .putString(KEY_LAST_APPLY, today.toString())
            .putInt(KEY_STREAK, newStreak)
            .apply()
    }

    fun getStreak(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getInt(KEY_STREAK, 0)
    }
}
