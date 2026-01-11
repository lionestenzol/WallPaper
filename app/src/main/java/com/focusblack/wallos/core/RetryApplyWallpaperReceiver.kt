package com.focusblack.wallos.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.focusblack.wallos.util.ErrorNotifier
import com.focusblack.wallos.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RetryApplyWallpaperReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_RETRY_APPLY) {
            return
        }
        val wallId = intent.getStringExtra(EXTRA_WALL_ID)
        if (wallId.isNullOrBlank()) {
            Log.e(TAG, "Retry apply failed: missing wall id")
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            val wall = PackRegistry.findWallById(wallId)
            if (wall == null) {
                Log.e(TAG, "Retry apply failed: wall not found id=$wallId")
                pendingResult.finish()
                return@launch
            }
            val applied = WallpaperEngine.applyWall(context, wall) { failure ->
                ErrorNotifier.showApplyFailureNotification(context, wall, failure.userMessage)
            }
            if (applied) {
                WidgetUpdater.updateAll(context)
            }
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "RetryApplyReceiver"
        const val ACTION_RETRY_APPLY = "com.focusblack.wallos.action.RETRY_APPLY_WALLPAPER"
        const val EXTRA_WALL_ID = "extra_wall_id"

        fun createIntent(context: Context, wallId: String): Intent {
            return Intent(context, RetryApplyWallpaperReceiver::class.java).apply {
                action = ACTION_RETRY_APPLY
                putExtra(EXTRA_WALL_ID, wallId)
            }
        }
    }
}
