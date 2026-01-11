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
            val connection = openHttpsConnection(url) ?: return@withContext null
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@withContext null
                }
                val contentLength = connection.contentLengthLong
                if (contentLength <= 0 || contentLength > MAX_ASSET_SIZE_BYTES) {
                    return@withContext null
                }
                connection.inputStream.use { input ->
                    val buffered = BufferedInputStream(input)
                    return@withContext diskCache.put(url) { file ->
                        FileOutputStream(file).use { output ->
                            buffered.copyTo(output)
                        }
                    }
                }
            } finally {
                connection.disconnect()
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
        private const val MAX_ASSET_SIZE_BYTES = 25L * 1024L * 1024L
        private const val TIMEOUT_MS = 15_000
    }

    private fun openHttpsConnection(url: String): HttpURLConnection? {
        val parsedUrl = runCatching { URL(url) }.getOrNull() ?: return null
        if (!parsedUrl.protocol.equals("https", ignoreCase = true)) {
            return null
        }
        val connection = parsedUrl.openConnection() as? HttpURLConnection ?: return null
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.instanceFollowRedirects = false
        return connection
    }
}
