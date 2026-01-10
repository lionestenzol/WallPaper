package com.focusblack.wallos.widget

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.focusblack.wallos.R

class BatteryHaloWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context?, appWidgetManager: android.appwidget.AppWidgetManager?, appWidgetIds: IntArray?) {
        context ?: return
        appWidgetIds ?: return
        val rv = RemoteViews(context.packageName, R.layout.fragment_widgets)
        for (id in appWidgetIds) {
            appWidgetManager?.updateAppWidget(id, rv)
        }
    }
}
