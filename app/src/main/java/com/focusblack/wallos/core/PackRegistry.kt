package com.focusblack.wallos.core

import com.focusblack.wallos.model.Pack
import com.focusblack.wallos.model.Wall

object PackRegistry {
    private val packs = mutableMapOf<String, Pack>()

    init {
        // Register GENESIS_001 with a single wall
        val genesis = Pack(
            id = "GENESIS_001",
            title = "Genesis Pack",
            sku = "pack_genesis_001",
            walls = listOf(
                Wall(id = "genesis_prime", title = "Genesis Prime", drawableName = "genesis_prime")
            )
        )
        packs[genesis.id] = genesis
    }

    fun getPack(id: String): Pack? = packs[id]

    fun listPacks(): List<Pack> = packs.values.toList()
}
