package com.focusblack.wallos.core

import com.focusblack.wallos.model.Pack
import com.focusblack.wallos.model.Wall

object PackRegistry {
    private val packs = mutableMapOf<String, Pack>()

    init {
        // Register GENESIS_001 with all wallpapers
        val genesis = Pack(
            id = "GENESIS_001",
            title = "Genesis Pack",
            sku = "pack_genesis_001",
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
}
