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
) {
    /** Returns [imgUrl] if available, otherwise a BrickLink CDN fallback based on type and ID. */
    val displayImgUrl: String
        get() = imgUrl ?: when (type) {
            "part" -> "https://img.bricklink.com/ItemImage/PN/0/$id.png"
            "set"  -> "https://img.bricklink.com/ItemImage/SN/0/$id.png"
            "fig"  -> "https://img.bricklink.com/ItemImage/MN/0/$id.png"
            else   -> "https://img.bricklink.com/ItemImage/PN/0/$id.png"
        }
}
