package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.focusblack.wallos.model.Wall

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    fun applyWall(context: Context, wall: Wall): Boolean {
        return try {
            WallosLogger.info(
                TAG,
                "wallpaper_apply_started",
                mapOf(
                    "wall_id" to wall.id,
                    "title" to wall.title,
                    "drawable" to wall.drawableName
                )
            )
            WallosAnalytics.track(
                "wallpaper_apply_started",
                mapOf(
                    "wall_id" to wall.id,
                    "title" to wall.title
                )
            )

            // Get drawable resource ID from name
            val resourceId = context.resources.getIdentifier(
                wall.drawableName,
                "drawable",
                context.packageName
            )

            if (resourceId == 0) {
                WallosLogger.warn(
                    TAG,
                    "wallpaper_drawable_missing",
                    mapOf("drawable" to wall.drawableName)
                )
                WallosAnalytics.track(
                    "wallpaper_apply_failed",
                    mapOf(
                        "wall_id" to wall.id,
                        "reason" to "drawable_missing"
                    )
                )
                return false
            }

            // Load drawable and convert to bitmap
            val drawable = ContextCompat.getDrawable(context, resourceId)
            if (drawable == null) {
                WallosLogger.warn(
                    TAG,
                    "wallpaper_drawable_load_failed",
                    mapOf("drawable" to wall.drawableName)
                )
                WallosAnalytics.track(
                    "wallpaper_apply_failed",
                    mapOf(
                        "wall_id" to wall.id,
                        "reason" to "drawable_load_failed"
                    )
                )
                return false
            }

            val bitmap = drawableToBitmap(context, drawable)

            // Set as wallpaper
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)

            WallosLogger.info(
                TAG,
                "wallpaper_apply_succeeded",
                mapOf(
                    "wall_id" to wall.id,
                    "title" to wall.title
                )
            )
            WallosAnalytics.track(
                "wallpaper_apply_succeeded",
                mapOf("wall_id" to wall.id)
            )
            true
        } catch (e: Exception) {
            WallosLogger.error(
                TAG,
                "wallpaper_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "title" to wall.title
                ),
                e
            )
            WallosAnalytics.track(
                "wallpaper_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "reason" to "exception"
                )
            )
            false
        }
    }

    private fun drawableToBitmap(context: Context, drawable: Drawable): Bitmap {
        val wallpaperManager = WallpaperManager.getInstance(context)
        val metrics = context.resources.displayMetrics
        val width = (wallpaperManager.desiredMinimumWidth.takeIf { it > 0 } ?: metrics.widthPixels)
            .coerceAtLeast(1)
        val height = (wallpaperManager.desiredMinimumHeight.takeIf { it > 0 } ?: metrics.heightPixels)
            .coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }
}
