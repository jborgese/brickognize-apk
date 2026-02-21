package com.frootsnoops.brickognize.domain.model

data class RecognitionResult(
    val listingId: String,
    val topCandidate: BrickItem?,
    val candidates: List<BrickItem>,
    val timestamp: Long = System.currentTimeMillis()
)
