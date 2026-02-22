package com.frootsnoops.brickognize.domain.model

data class BrickItem(
    val id: String,
    val name: String,
    val type: String, // "part", "set", "fig"
    val category: String? = null,
    val imgUrl: String? = null,
    val score: Double? = null,
    val binLocation: BinLocation? = null,
    val binLocations: List<BinLocation> = binLocation?.let { listOf(it) } ?: emptyList()
)
