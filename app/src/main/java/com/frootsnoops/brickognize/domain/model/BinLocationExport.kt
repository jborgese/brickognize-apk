package com.frootsnoops.brickognize.domain.model

import kotlinx.serialization.Serializable

/**
 * Serializable representation of bin locations for export/import
 */
@Serializable
data class BinLocationExport(
    val label: String,
    val description: String? = null,
    val createdAt: Long
)

@Serializable
data class PartExport(
    val id: String,
    val name: String,
    val type: String,
    val category: String? = null,
    val imgUrl: String? = null,
    val binLabel: String? = null, // Legacy single-bin field (v2 and below)
    val binLabels: List<String>? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class BinLocationsBackup(
    val version: Int = 3,
    val exportedAt: Long,
    val binLocations: List<BinLocationExport>,
    val parts: List<PartExport>? = null
)
