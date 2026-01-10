package com.focusblack.wallos.core

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.focusblack.wallos.model.Pack

class RotationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Minimal rotation: apply the first wall from GENESIS pack if present
        val pack = PackRegistry.getPack("GENESIS_001")
        val wall = pack?.walls?.firstOrNull()
        if (wall != null) {
            WallpaperEngine.applyWall(applicationContext, wall)
            StreakEngine.onDailyApplied(applicationContext)
        }
        return Result.success()
    }
}
