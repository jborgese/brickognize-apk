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
        get() = imgUrl ?: brickLinkImgUrl

    /** BrickLink catalog image URL used as a fallback when [imgUrl] is missing or stale. */
    val brickLinkImgUrl: String
        get() = when (type) {
            "part" -> "https://www.bricklink.com/PL/$id.jpg"
            "set"  -> "https://www.bricklink.com/SL/$id.jpg"
            "fig"  -> "https://www.bricklink.com/ML/$id.jpg"
            else   -> "https://www.bricklink.com/PL/$id.jpg"
        }
}
