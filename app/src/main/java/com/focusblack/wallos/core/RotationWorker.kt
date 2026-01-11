package com.focusblack.wallos.core

import android.content.Context
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
        val startMillis = System.currentTimeMillis()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val pack = PackRegistry.getPack("GENESIS_001")

        if (pack == null || pack.walls.isEmpty()) {
            WallosLogger.warn(
                TAG,
                "rotation_pack_unavailable",
                mapOf("pack_id" to "GENESIS_001")
            )
            WallosAnalytics.track(
                "rotation_pack_unavailable",
                mapOf("pack_id" to "GENESIS_001")
            )
            return Result.failure()
        }

        // Get current index and cycle to next wallpaper
        val currentIndex = prefs.getInt(KEY_CURRENT_WALL_INDEX, 0)
        val nextIndex = (currentIndex + 1) % pack.walls.size
        val wall = pack.walls[currentIndex]

        // Apply wallpaper
        val applied = WallpaperEngine.applyWall(applicationContext, wall)
        if (!applied) {
            WallosLogger.warn(
                TAG,
                "rotation_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "index" to currentIndex,
                    "pack_id" to pack.id
                )
            )
            WallosAnalytics.track(
                "rotation_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "index" to currentIndex,
                    "pack_id" to pack.id
                )
            )
            return Result.retry()
        }
        StreakEngine.onDailyApplied(applicationContext)

        // Save next index for next rotation
        prefs.edit { putInt(KEY_CURRENT_WALL_INDEX, nextIndex) }
        WidgetUpdater.updateAll(applicationContext)

        val durationMs = System.currentTimeMillis() - startMillis
        WallosLogger.info(
            TAG,
            "rotation_apply_succeeded",
            mapOf(
                "wall_id" to wall.id,
                "index" to currentIndex,
                "next_index" to nextIndex,
                "pack_id" to pack.id,
                "duration_ms" to durationMs
            )
        )
        WallosAnalytics.track(
            "rotation_apply_succeeded",
            mapOf(
                "wall_id" to wall.id,
                "index" to currentIndex,
                "next_index" to nextIndex,
                "pack_id" to pack.id,
                "duration_ms" to durationMs
            )
        )

        return Result.success()
    }

    companion object {
        private const val KEY_CURRENT_WALL_INDEX = "rotation_current_index"
        private const val TAG = "RotationWorker"
    }
}
