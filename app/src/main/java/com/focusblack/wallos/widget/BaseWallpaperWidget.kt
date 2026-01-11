package com.focusblack.wallos.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseWallpaperWidget(
    private val layoutId: Int
) : AppWidgetProvider() {

    override fun onUpdate(
        context: Context?,
        appWidgetManager: android.appwidget.AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        context ?: return
        appWidgetManager ?: return
        appWidgetIds ?: return
        for (id in appWidgetIds) {
            val rv = WidgetViews.build(context, layoutId)
            appWidgetManager.updateAppWidget(id, rv)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
        context ?: return
        if (intent?.action == ACTION_APPLY_NOW) {
            if (!isTrustedCaller(context)) {
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

    companion object {
        const val ACTION_APPLY_NOW = "com.focusblack.wallos.widget.ACTION_APPLY_NOW"
    }

    private fun isTrustedCaller(context: Context): Boolean {
        val callingUid = android.os.Binder.getCallingUid()
        val myUid = android.os.Process.myUid()
        if (callingUid == myUid) {
            return true
        }
        return context.packageManager.checkSignatures(callingUid, myUid) ==
            android.content.pm.PackageManager.SIGNATURE_MATCH
    }
}
