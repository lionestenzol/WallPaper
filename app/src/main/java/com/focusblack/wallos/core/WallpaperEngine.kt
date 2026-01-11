package com.focusblack.wallos.core

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.focusblack.wallos.data.cache.WallpaperAssetCache
import com.focusblack.wallos.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"
    const val KEY_LAST_APPLY_RESULT = "wallpaper_last_apply_result"
    const val KEY_LAST_APPLY_ERROR = "wallpaper_last_apply_error"
    const val KEY_LAST_APPLY_TIME = "wallpaper_last_apply_time"

    data class ApplyResult(
        val success: Boolean,
        val error: String? = null
    )

    suspend fun applyWall(context: Context, wall: Wall): ApplyResult {
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

            val bitmap = loadBitmap(context, wall)
            if (bitmap == null) {
                val errorMsg = "bitmap_load_failed"
                WallosLogger.warn(
                    TAG,
                    "wallpaper_load_failed",
                    mapOf("wall_id" to wall.id, "drawable" to wall.drawableName)
                )
                WallosAnalytics.track(
                    "wallpaper_apply_failed",
                    mapOf(
                        "wall_id" to wall.id,
                        "reason" to errorMsg
                    )
                )
                return ApplyResult(false, errorMsg)
            }


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
            ApplyResult(true)
        } catch (e: Exception) {
            val errorMsg = e.message ?: e.javaClass.simpleName
            WallosLogger.error(
                TAG,
                "wallpaper_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "title" to wall.title,
                    "error" to errorMsg
                ),
                e
            )
            WallosAnalytics.track(
                "wallpaper_apply_failed",
                mapOf(
                    "wall_id" to wall.id,
                    "reason" to "exception",
                    "error" to errorMsg
                )
            )
            ApplyResult(false, errorMsg)
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
            WallosLogger.warn(TAG, "drawable_not_found", mapOf("drawable" to wall.drawableName))
            return null
        }

        val drawable = ContextCompat.getDrawable(context, resourceId)
        if (drawable == null) {
            WallosLogger.warn(TAG, "drawable_load_failed", mapOf("drawable" to wall.drawableName))
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
