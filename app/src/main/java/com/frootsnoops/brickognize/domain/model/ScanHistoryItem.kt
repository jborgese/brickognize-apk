package com.frootsnoops.brickognize.domain.model

data class ScanHistoryItem(
    val scanId: Long,
    val timestamp: Long,
    val imagePath: String?,
    val topItem: BrickItem?,
    val candidateCount: Int,
    val recognitionType: String
)
