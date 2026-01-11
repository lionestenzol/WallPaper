package com.focusblack.wallos.core

import android.content.Context
import android.util.Log
import com.focusblack.wallos.data.PackRepository
import com.focusblack.wallos.model.Pack
import com.focusblack.wallos.model.Wall
import com.focusblack.wallos.util.ErrorNotifier

object PackRegistry {
    private const val TAG = "PackRegistry"
    private val packs = mutableMapOf<String, Pack>()

    init {
        // Register GENESIS_001 with all wallpapers
        val genesis = Pack(
            id = "GENESIS_001",
            title = "Genesis Pack",
            sku = "pack_genesis_001",
            previewImages = listOf("genesis_prime"),
            walls = listOf(
                Wall(id = "genesis_prime", title = "Genesis Prime", drawableName = "genesis_prime"),
                Wall(id = "genesis_seal", title = "Genesis Seal", drawableName = "genesis_seal"),
                Wall(id = "wall_horizon", title = "Horizon", drawableName = "wall_horizon"),
                Wall(id = "wall_circle", title = "Circle", drawableName = "wall_circle"),
                Wall(id = "wall_crescent", title = "Crescent", drawableName = "wall_crescent"),
                Wall(id = "wall_grid", title = "Grid", drawableName = "wall_grid"),
                Wall(id = "wall_accent_line", title = "Accent Line", drawableName = "wall_accent_line")
            )
        )
        packs[genesis.id] = genesis
    }

    fun getPack(id: String): Pack? = packs[id]

    fun listPacks(): List<Pack> = packs.values.toList()

    fun upsertPacks(remotePacks: List<Pack>) {
        remotePacks.forEach { pack ->
            packs[pack.id] = pack
        }
    }

    fun findWallById(id: String): Wall? {
        return packs.values.asSequence()
            .mapNotNull { pack -> pack.walls.firstOrNull { it.id == id } }
            .firstOrNull()
    }

    suspend fun loadRemotePacks(
        context: Context,
        url: String,
        repository: PackRepository = PackRepository()
    ): List<Pack> {
        val result = repository.fetchRemotePacks(url)
        return result.fold(
            onSuccess = { remotePacks ->
                upsertPacks(remotePacks)
                remotePacks
            },
            onFailure = { error ->
                Log.e(TAG, "Remote pack load failed. url=$url", error)
                ErrorNotifier.showPackFetchFailureNotification(context, url)
                emptyList()
            }
        )
    }
}
