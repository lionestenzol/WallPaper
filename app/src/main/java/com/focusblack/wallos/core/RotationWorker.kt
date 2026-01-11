package com.focusblack.wallos.core

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RotationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val pack = PackRegistry.getPack("GENESIS_001")

        if (pack == null || pack.walls.isEmpty()) {
            return Result.failure()
        }

        // Get current index and cycle to next wallpaper
        val currentIndex = prefs.getInt(KEY_CURRENT_WALL_INDEX, 0)
        val nextIndex = (currentIndex + 1) % pack.walls.size
        val wall = pack.walls[currentIndex]

        // Apply wallpaper
        WallpaperEngine.applyWall(applicationContext, wall)
        StreakEngine.onDailyApplied(applicationContext)

        // Save next index for next rotation
        prefs.edit { putInt(KEY_CURRENT_WALL_INDEX, nextIndex) }

        return Result.success()
    }

    companion object {
        private const val KEY_CURRENT_WALL_INDEX = "rotation_current_index"
    }
}
