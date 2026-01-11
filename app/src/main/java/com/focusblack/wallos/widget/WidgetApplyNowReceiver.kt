package com.focusblack.wallos.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetApplyNowReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action != BaseWallpaperWidget.ACTION_APPLY_NOW) {
            return
        }
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            WidgetDataSource.applyNow(context)
            WidgetUpdater.updateAll(context)
            pendingResult.finish()
        }
    }
}
