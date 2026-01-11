package com.focusblack.wallos.data

import com.focusblack.wallos.model.Pack
import com.focusblack.wallos.model.Wall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class PackRepository {

    suspend fun fetchRemotePacks(url: String): List<Pack> = withContext(Dispatchers.IO) {
        runCatching {
            val connection = openHttpsConnection(url) ?: return@withContext emptyList()
            try {
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    return@withContext emptyList()
                }
                val contentLength = connection.contentLengthLong
                if (contentLength <= 0 || contentLength > MAX_RESPONSE_SIZE_BYTES) {
                    return@withContext emptyList()
                }
                val body = connection.inputStream.bufferedReader().use { it.readText() }
                parsePacks(body)
            } finally {
                connection.disconnect()
            }
        }.getOrElse { emptyList() }
    }

    private fun parsePacks(json: String): List<Pack> {
        val root = JSONObject(json)
        val packs = root.optJSONArray("packs") ?: JSONArray()
        return List(packs.length()) { index ->
            val packJson = packs.getJSONObject(index)
            Pack(
                id = packJson.getString("id"),
                title = packJson.getString("title"),
                sku = packJson.optString("sku", packJson.getString("id")),
                previewImages = packJson.optJSONArray("previewImages")?.toStringList().orEmpty(),
                walls = packJson.optJSONArray("walls")?.toWallList().orEmpty()
            )
        }
    }

    private fun JSONArray.toStringList(): List<String> {
        return List(length()) { index -> getString(index) }
    }

    private fun JSONArray.toWallList(): List<Wall> {
        return List(length()) { index ->
            val wallJson = getJSONObject(index)
            val assetUrl = wallJson.optString("assetUrl").takeIf { it.isNotBlank() }
            Wall(
                id = wallJson.getString("id"),
                title = wallJson.getString("title"),
                drawableName = wallJson.optString("drawableName", ""),
                assetUrl = assetUrl
            )
        }
    }

    companion object {
        private const val MAX_RESPONSE_SIZE_BYTES = 5L * 1024L * 1024L
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
