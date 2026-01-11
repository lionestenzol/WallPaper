package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.focusblack.wallos.model.Wall

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"
    const val KEY_LAST_APPLY_RESULT = "wallpaper_last_apply_result"
    const val KEY_LAST_APPLY_ERROR = "wallpaper_last_apply_error"
    const val KEY_LAST_APPLY_TIME = "wallpaper_last_apply_time"

    data class ApplyResult(
        val success: Boolean,
        val error: String? = null
    )

    fun applyWall(context: Context, wall: Wall): ApplyResult {
        return try {
            Log.i(TAG, "Applying wall: ${wall.id} title=${wall.title}")

            // Get drawable resource ID from name
            val resourceId = context.resources.getIdentifier(
                wall.drawableName,
                "drawable",
                context.packageName
            )

            if (resourceId == 0) {
                Log.e(TAG, "Drawable not found: ${wall.drawableName}")
                return ApplyResult(false, "drawable_not_found")
            }

            // Load drawable and convert to bitmap
            val drawable = ContextCompat.getDrawable(context, resourceId)
            if (drawable == null) {
                Log.e(TAG, "Failed to load drawable: ${wall.drawableName}")
                return ApplyResult(false, "drawable_load_failed")
            }

            val bitmap = drawableToBitmap(context, drawable)

            // Set as wallpaper
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)

            Log.i(TAG, "Successfully applied wallpaper: ${wall.title}")
            ApplyResult(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper: ${wall.title}", e)
            ApplyResult(false, e.message ?: e.javaClass.simpleName)
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
