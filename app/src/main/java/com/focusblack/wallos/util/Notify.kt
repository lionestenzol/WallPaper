package com.focusblack.wallos.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object Notify {
    const val CHANNEL_ID = "daily_rotation"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Rotation"
            val desc = "Daily wallpaper rotation notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val ch = NotificationChannel(CHANNEL_ID, name, importance).apply { description = desc }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(ch)
        }
    }
}
