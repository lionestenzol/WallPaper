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

    fun applyWall(context: Context, wall: Wall) {
        try {
            Log.i(TAG, "Applying wall: ${wall.id} title=${wall.title}")

            // Get drawable resource ID from name
            val resourceId = context.resources.getIdentifier(
                wall.drawableName,
                "drawable",
                context.packageName
            )

            if (resourceId == 0) {
                Log.e(TAG, "Drawable not found: ${wall.drawableName}")
                return
            }

            // Load drawable and convert to bitmap
            val drawable = ContextCompat.getDrawable(context, resourceId)
            if (drawable == null) {
                Log.e(TAG, "Failed to load drawable: ${wall.drawableName}")
                return
            }

            val bitmap = drawableToBitmap(drawable)

            // Set as wallpaper
            val wallpaperManager = WallpaperManager.getInstance(context)
            wallpaperManager.setBitmap(bitmap)

            Log.i(TAG, "Successfully applied wallpaper: ${wall.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply wallpaper: ${wall.title}", e)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        // Use reasonable wallpaper dimensions
        val width = 1080
        val height = 2340

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)

        return bitmap
    }
}
