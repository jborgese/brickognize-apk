package com.frootsnoops.brickognize.data.remote.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class LegacySearchResultsDto(
    @SerialName("listing_id")
    val listingId: String,

    @SerialName("bounding_box")
    val boundingBox: BoundingBoxDto,

    @SerialName("items")
    val items: List<LegacyCandidateItemDto>
)

@Keep
@Serializable
data class BoundingBoxDto(
    @SerialName("x1")
    val x1: Double,

    @SerialName("y1")
    val y1: Double,

    @SerialName("x2")
    val x2: Double,

    @SerialName("y2")
    val y2: Double
)

@Keep
@Serializable
data class LegacyCandidateItemDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("img_url")
    val imgUrl: String,

    @SerialName("external_sites")
    val externalSites: List<LegacyExternalSiteDto>? = null,

    @SerialName("category")
    val category: String? = null,

    @SerialName("type")
    val type: String,

    @SerialName("score")
    val score: Double
)

@Keep
@Serializable
data class LegacyExternalSiteDto(
    @SerialName("site")
    val site: String,

    @SerialName("url")
    val url: String
)
