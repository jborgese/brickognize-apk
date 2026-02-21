package com.frootsnoops.brickognize.domain.model

data class BinLocation(
    val id: Long,
    val label: String,
    val description: String? = null,
    val createdAt: Long
)
