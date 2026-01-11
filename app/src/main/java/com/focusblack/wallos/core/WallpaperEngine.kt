package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.focusblack.wallos.model.Wall

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    enum class ApplyTarget(val prefValue: String) {
        SYSTEM("system"),
        LOCK("lock"),
        BOTH("both");

        companion object {
            fun fromPreference(value: String?): ApplyTarget {
                return values().firstOrNull { it.prefValue == value } ?: BOTH
            }
        }
    }

    fun applyWall(context: Context, wall: Wall, target: ApplyTarget): Boolean {
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
                return false
            }

            // Load drawable and convert to bitmap
            val drawable = ContextCompat.getDrawable(context, resourceId)
            if (drawable == null) {
                Log.e(TAG, "Failed to load drawable: ${wall.drawableName}")
                return false
            }

            val bitmap = drawableToBitmap(context, drawable)

            // Set as wallpaper
            val wallpaperManager = WallpaperManager.getInstance(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val which = when (target) {
                    ApplyTarget.SYSTEM -> WallpaperManager.FLAG_SYSTEM
                    ApplyTarget.LOCK -> WallpaperManager.FLAG_LOCK
                    ApplyTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setBitmap(bitmap, null, true, which)
            } else {
                wallpaperManager.setBitmap(bitmap)
            }

            Log.i(TAG, "Successfully applied wallpaper: ${wall.title}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper: ${wall.title}", e)
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
