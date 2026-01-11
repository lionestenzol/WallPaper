package com.focusblack.wallos.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import androidx.annotation.LayoutRes
import com.focusblack.wallos.R

object WidgetUpdater {
    private data class WidgetDefinition(
        val provider: Class<out AppWidgetProvider>,
        @LayoutRes val layoutId: Int
    )

    private val widgets = listOf(
        WidgetDefinition(FocusRingWidget::class.java, R.layout.widget_focus_ring),
        WidgetDefinition(DateGlyphWidget::class.java, R.layout.widget_date_glyph),
        WidgetDefinition(ModeSigilWidget::class.java, R.layout.widget_mode_sigil),
        WidgetDefinition(BatteryHaloWidget::class.java, R.layout.widget_battery_halo)
    )

    fun updateAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        for (widget in widgets) {
            val ids = manager.getAppWidgetIds(ComponentName(context, widget.provider))
            if (ids.isNotEmpty()) {
                val views = WidgetViews.build(context, widget.layoutId)
                manager.updateAppWidget(ids, views)
            }
        }
    }
}
