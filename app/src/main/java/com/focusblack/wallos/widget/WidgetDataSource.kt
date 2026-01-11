package com.focusblack.wallos.widget

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackRegistry
import com.focusblack.wallos.core.StreakEngine
import com.focusblack.wallos.core.WallpaperEngine

object WidgetDataSource {
    private const val KEY_CURRENT_INDEX = "rotation_current_index"

    fun getState(context: Context): WidgetState {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val pack = PackRegistry.getPack("GENESIS_001")
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val wall = pack?.walls?.getOrNull(currentIndex)
        val title = wall?.title ?: context.getString(R.string.widget_wallpaper_unknown)
        val streak = StreakEngine.getStreak(context)
        return WidgetState(title, streak)
    }

    fun applyNow(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val pack = PackRegistry.getPack("GENESIS_001") ?: return false
        if (pack.walls.isEmpty()) {
            return false
        }
        val currentIndex = prefs.getInt(KEY_CURRENT_INDEX, 0)
        val wall = pack.walls.getOrNull(currentIndex) ?: return false
        val result = WallpaperEngine.applyWall(context, wall)
        if (result.success) {
            StreakEngine.onDailyApplied(context)
            val nextIndex = (currentIndex + 1) % pack.walls.size
            prefs.edit { putInt(KEY_CURRENT_INDEX, nextIndex) }
        }
        return result.success
    }
}
