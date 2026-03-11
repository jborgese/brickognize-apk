package com.frootsnoops.brickognize.util

/**
 * Constructs a stable BrickLink CDN image URL from a part's type and ID.
 * Unlike Brickognize CDN URLs (which include versioned paths that break over time),
 * BrickLink CDN URLs are deterministic and stable.
 */
fun brickLinkImgUrl(type: String, id: String): String = when (type) {
    "part" -> "https://www.bricklink.com/PL/$id.jpg"
    "set"  -> "https://www.bricklink.com/SL/$id.jpg"
    "fig"  -> "https://www.bricklink.com/ML/$id.jpg"
    else   -> "https://www.bricklink.com/PL/$id.jpg"
}

/**
 * Returns true if the URL points to the Brickognize CDN, which uses versioned
 * paths (e.g. `thumbnails-v2.18`) that go stale when the CDN version changes.
 */
fun isStaleCandidate(imgUrl: String?): Boolean {
    if (imgUrl == null) return false
    return imgUrl.contains("brickognize", ignoreCase = true)
}

/**
 * Normalizes a part's image URL: replaces stale Brickognize CDN URLs with the
 * stable BrickLink CDN equivalent. Returns null if the original URL was null
 * (the display layer will compute the fallback at render time).
 */
fun normalizeImgUrl(imgUrl: String?, type: String, id: String): String? {
    if (imgUrl == null) return null
    return if (isStaleCandidate(imgUrl)) brickLinkImgUrl(type, id) else imgUrl
}
