package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import com.focusblack.wallos.data.cache.WallpaperAssetCache
import com.focusblack.wallos.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    suspend fun applyWall(context: Context, wall: Wall): Boolean {
        return try {
            Log.i(TAG, "Applying wall: ${wall.id} title=${wall.title}")

            val bitmap = loadBitmap(context, wall) ?: return false

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

    private suspend fun loadBitmap(context: Context, wall: Wall): Bitmap? {
        val assetUrl = wall.assetUrl
        if (!assetUrl.isNullOrBlank()) {
            val cache = WallpaperAssetCache(context.applicationContext)
            val file = cache.getOrDownload(assetUrl) ?: return null
            return withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
        }

        val resourceId = context.resources.getIdentifier(
            wall.drawableName,
            "drawable",
            context.packageName
        )
        if (resourceId == 0) {
            Log.e(TAG, "Drawable not found: ${wall.drawableName}")
            return null
        }

        val drawable = ContextCompat.getDrawable(context, resourceId)
        if (drawable == null) {
            Log.e(TAG, "Failed to load drawable: ${wall.drawableName}")
            return null
        }

        return drawableToBitmap(context, drawable)
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
