package com.focusblack.wallos.model

data class Pack(
    val id: String,
    val title: String,
    val sku: String,
    val walls: List<Wall>
)
