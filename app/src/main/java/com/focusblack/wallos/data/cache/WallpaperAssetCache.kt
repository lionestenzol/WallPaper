package com.focusblack.wallos.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class WallpaperAssetCache(context: Context) {

    private val diskCache = DiskLruCache(
        directory = File(context.cacheDir, "wallpaper_assets"),
        maxSizeBytes = MAX_CACHE_SIZE_BYTES
    )

    suspend fun getOrDownload(url: String): File? = withContext(Dispatchers.IO) {
        diskCache.get(url)?.let { return@withContext it }
        runCatching {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.inputStream.use { input ->
                val buffered = BufferedInputStream(input)
                return@withContext diskCache.put(url) { file ->
                    FileOutputStream(file).use { output ->
                        buffered.copyTo(output)
                    }
                }
            }
        }.getOrNull()
    }

    suspend fun loadBitmap(url: String): Bitmap? {
        val file = getOrDownload(url) ?: return null
        return withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(file.absolutePath)
        }
    }

    companion object {
        private const val MAX_CACHE_SIZE_BYTES = 100L * 1024L * 1024L
        private const val TIMEOUT_MS = 15_000
    }
}
