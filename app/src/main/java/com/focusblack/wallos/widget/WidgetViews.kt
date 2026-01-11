package com.focusblack.wallos.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.LayoutRes
import com.focusblack.wallos.R

object WidgetViews {
    fun build(
        context: Context,
        @LayoutRes layoutId: Int
    ): RemoteViews {
        val state = WidgetDataSource.getState(context)
        val views = RemoteViews(context.packageName, layoutId)
        views.setTextViewText(
            R.id.widget_wall_title,
            context.getString(R.string.widget_wall_title_fmt, state.wallTitle)
        )
        views.setTextViewText(
            R.id.widget_streak,
            context.getString(R.string.streak_fmt, state.streak)
        )
        val intent = Intent(context, WidgetApplyNowReceiver::class.java).apply {
            action = BaseWallpaperWidget.ACTION_APPLY_NOW
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_apply_button, pendingIntent)
        return views
    }
}
