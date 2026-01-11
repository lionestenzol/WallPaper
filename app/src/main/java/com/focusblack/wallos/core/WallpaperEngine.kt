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
import com.focusblack.wallos.R
import com.focusblack.wallos.data.cache.WallpaperAssetCache
import com.focusblack.wallos.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    data class ApplyFailure(
        val userMessage: String,
        val logMessage: String,
        val throwable: Throwable? = null
    )

    suspend fun applyWall(
        context: Context,
        wall: Wall,
        onFailure: ((ApplyFailure) -> Unit)? = null
    ): Boolean {
        return try {
            Log.i(TAG, "Applying wall: ${wall.id} title=${wall.title}")

            val loadResult = loadBitmap(context, wall)
            val bitmap = when (loadResult) {
                is LoadBitmapResult.Success -> loadResult.bitmap
                is LoadBitmapResult.Failure -> {
                    val failure = ApplyFailure(
                        userMessage = loadResult.userMessage,
                        logMessage = loadResult.logMessage,
                        throwable = loadResult.throwable
                    )
                    Log.e(TAG, failure.logMessage, failure.throwable)
                    onFailure?.invoke(failure)
                    return false
                }
            }

            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)

            Log.i(TAG, "Successfully applied wallpaper: ${wall.title}")
            true
        } catch (e: Exception) {
            val failure = ApplyFailure(
                userMessage = context.getString(R.string.error_apply_failed),
                logMessage = "Failed to apply wallpaper id=${wall.id} title=${wall.title}",
                throwable = e
            )
            Log.e(TAG, failure.logMessage, failure.throwable)
            onFailure?.invoke(failure)
            false
        }
    }

    private sealed class LoadBitmapResult {
        data class Success(val bitmap: Bitmap) : LoadBitmapResult()
        data class Failure(
            val userMessage: String,
            val logMessage: String,
            val throwable: Throwable? = null
        ) : LoadBitmapResult()
    }

    private suspend fun loadBitmap(context: Context, wall: Wall): LoadBitmapResult {
        val assetUrl = wall.assetUrl
        if (!assetUrl.isNullOrBlank()) {
            val cache = WallpaperAssetCache(context.applicationContext)
            val fileResult = cache.getOrDownload(assetUrl)
            return fileResult.fold(
                onSuccess = { file ->
                    val bitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    }
                    if (bitmap == null) {
                        LoadBitmapResult.Failure(
                            userMessage = context.getString(R.string.error_wallpaper_decode_failed),
                            logMessage = "Failed to decode wallpaper. id=${wall.id} url=$assetUrl file=${file.absolutePath}"
                        )
                    } else {
                        LoadBitmapResult.Success(bitmap)
                    }
                },
                onFailure = { error ->
                    LoadBitmapResult.Failure(
                        userMessage = context.getString(R.string.error_wallpaper_download_failed),
                        logMessage = "Failed to download wallpaper. id=${wall.id} url=$assetUrl",
                        throwable = error
                    )
                }
            )
        }

        val drawableName = wall.drawableName
        val resourceId = context.resources.getIdentifier(
            drawableName,
            "drawable",
            context.packageName
        )
        if (resourceId == 0) {
            return LoadBitmapResult.Failure(
                userMessage = context.getString(R.string.error_wallpaper_not_found),
                logMessage = "Drawable not found for wall id=${wall.id} drawable=$drawableName"
            )
        }

        val drawable = ContextCompat.getDrawable(context, resourceId)
        if (drawable == null) {
            return LoadBitmapResult.Failure(
                userMessage = context.getString(R.string.error_wallpaper_not_found),
                logMessage = "Failed to load drawable for wall id=${wall.id} drawable=$drawableName"
            )
        }

        return LoadBitmapResult.Success(drawableToBitmap(context, drawable))
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
