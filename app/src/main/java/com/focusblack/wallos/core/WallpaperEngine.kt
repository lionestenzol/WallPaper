package com.focusblack.wallos.core

import android.content.Context
import android.util.Log
import com.focusblack.wallos.model.Wall

object WallpaperEngine {
    private const val TAG = "WallpaperEngine"

    // Stub: apply a wall (in production would set system wallpaper or produce a live wallpaper)
    fun applyWall(context: Context, wall: Wall) {
        Log.i(TAG, "Applying wall: ${wall.id} title=${wall.title}")
        // TODO: real implementation using WallpaperManager or WallpaperService
    }
}
