package com.focusblack.wallos.core

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focusblack.wallos.widget.WidgetUpdater

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
        val lastIndex = pack.walls.lastIndex
        val safeIndex = if (currentIndex in 0..lastIndex) {
            currentIndex
        } else {
            Log.w(TAG, "Rotation index $currentIndex out of range (0..$lastIndex). Resetting to 0.")
            prefs.edit { putInt(KEY_CURRENT_WALL_INDEX, 0) }
            0
        }
        val nextIndex = (safeIndex + 1) % pack.walls.size
        val wall = pack.walls.getOrNull(safeIndex) ?: run {
            Log.w(TAG, "Rotation index $safeIndex invalid after correction. Resetting to 0.")
            prefs.edit { putInt(KEY_CURRENT_WALL_INDEX, 0) }
            pack.walls.first()
        }

        // Apply wallpaper
        val applied = WallpaperEngine.applyWall(applicationContext, wall)
        if (!applied) {
            return Result.retry()
        }
        StreakEngine.onDailyApplied(applicationContext)

        // Save next index for next rotation
        prefs.edit { putInt(KEY_CURRENT_WALL_INDEX, nextIndex) }
        WidgetUpdater.updateAll(applicationContext)

        return Result.success()
    }

    companion object {
        private const val KEY_CURRENT_WALL_INDEX = "rotation_current_index"
        private const val TAG = "RotationWorker"
    }
}
