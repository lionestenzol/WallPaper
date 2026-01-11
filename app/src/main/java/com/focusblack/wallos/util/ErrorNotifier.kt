package com.focusblack.wallos.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.focusblack.wallos.R
import com.focusblack.wallos.core.PackFetchRetryReceiver
import com.focusblack.wallos.core.RetryApplyWallpaperReceiver
import com.focusblack.wallos.model.Wall
import com.google.android.material.snackbar.Snackbar

object ErrorNotifier {
    private const val CHANNEL_ID = "wallos_errors"
    private const val CHANNEL_NAME = "Wallpaper errors"

    fun showRetrySnackbar(view: View, message: String, onRetry: () -> Unit) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(R.string.action_retry) { onRetry() }
            .show()
    }

    fun showApplyFailureNotification(context: Context, wall: Wall, message: String) {
        val intent = RetryApplyWallpaperReceiver.createIntent(context, wall.id)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            wall.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        showNotification(
            context = context,
            notificationId = wall.id.hashCode(),
            title = context.getString(R.string.notification_apply_failed_title),
            message = message,
            actionIntent = pendingIntent
        )
    }

    fun showPackFetchFailureNotification(context: Context, url: String) {
        val intent = PackFetchRetryReceiver.createIntent(context, url)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            url.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        showNotification(
            context = context,
            notificationId = url.hashCode(),
            title = context.getString(R.string.notification_pack_fetch_failed_title),
            message = context.getString(R.string.error_remote_packs_failed),
            actionIntent = pendingIntent
        )
    }

    private fun showNotification(
        context: Context,
        notificationId: Int,
        title: String,
        message: String,
        actionIntent: PendingIntent
    ) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(0, context.getString(R.string.action_retry), actionIntent)
            .build()
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }
}
