package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.focusblack.wallos.model.Wall

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    fun applyWall(context: Context, wall: Wall): Boolean {
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
            wallpaperManager.setBitmap(bitmap)

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
        val bounds = computeDrawableBounds(
            intrinsicWidth = drawable.intrinsicWidth,
            intrinsicHeight = drawable.intrinsicHeight,
            targetWidth = width,
            targetHeight = height,
            strategy = CropStrategy.CenterCrop
        )
        drawable.setBounds(bounds)
        drawable.draw(canvas)

        return bitmap
    }

    private enum class CropStrategy {
        CenterCrop
    }

    private fun computeDrawableBounds(
        intrinsicWidth: Int,
        intrinsicHeight: Int,
        targetWidth: Int,
        targetHeight: Int,
        strategy: CropStrategy
    ): Rect {
        if (intrinsicWidth <= 0 || intrinsicHeight <= 0) {
            return Rect(0, 0, targetWidth, targetHeight)
        }

        return when (strategy) {
            CropStrategy.CenterCrop -> {
                val scale = maxOf(
                    targetWidth.toFloat() / intrinsicWidth.toFloat(),
                    targetHeight.toFloat() / intrinsicHeight.toFloat()
                )
                val scaledWidth = (intrinsicWidth * scale).toInt()
                val scaledHeight = (intrinsicHeight * scale).toInt()
                val left = ((targetWidth - scaledWidth) / 2f).toInt()
                val top = ((targetHeight - scaledHeight) / 2f).toInt()
                Rect(left, top, left + scaledWidth, top + scaledHeight)
            }
        }
    }
}
