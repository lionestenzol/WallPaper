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
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parsePacks(body)
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
        private const val TIMEOUT_MS = 15_000
    }
}
