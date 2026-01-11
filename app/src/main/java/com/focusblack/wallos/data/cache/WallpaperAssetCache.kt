package com.focusblack.wallos.data.cache

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class WallpaperAssetCache(context: Context) {

    private val cacheDir = File(context.cacheDir, "wallpaper_assets")
    private val diskCache = DiskLruCache(
        directory = cacheDir,
        maxSizeBytes = MAX_CACHE_SIZE_BYTES
    )

    suspend fun getOrDownload(url: String): Result<File> = withContext(Dispatchers.IO) {
        diskCache.get(url)?.let { return@withContext Result.success(it) }
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.instanceFollowRedirects = true
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IOException("Unexpected HTTP $responseCode while downloading $url")
            }
            connection.inputStream.use { input ->
                val buffered = BufferedInputStream(input)
                val cachedFile = diskCache.put(url) { file ->
                    FileOutputStream(file).use { output ->
                        buffered.copyTo(output)
                    }
                }
                if (!cachedFile.exists() || cachedFile.length() == 0L) {
                    throw IOException("Cached file missing after download for $url")
                }
                Result.success(cachedFile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download asset. url=$url cacheDir=${cacheDir.absolutePath}", e)
            Result.failure(e)
        }
    }

    suspend fun loadBitmap(url: String): Result<Bitmap> {
        val fileResult = getOrDownload(url)
        return fileResult.mapCatching { file ->
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
            bitmap ?: throw IOException("Bitmap decode failed for $url file=${file.absolutePath}")
        }.onFailure { error ->
            Log.e(TAG, "Failed to load bitmap. url=$url", error)
        }
    }

    companion object {
        private const val TAG = "WallpaperAssetCache"
        private const val MAX_CACHE_SIZE_BYTES = 100L * 1024L * 1024L
        private const val TIMEOUT_MS = 15_000
    }
}
