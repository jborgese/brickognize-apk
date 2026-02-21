package com.frootsnoops.brickognize.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class LegacySearchResultsDto(
    @SerializedName("listing_id")
    val listingId: String,
    
    @SerializedName("bounding_box")
    val boundingBox: BoundingBoxDto,
    
    @SerializedName("items")
    val items: List<LegacyCandidateItemDto>
)

@Keep
data class BoundingBoxDto(
    @SerializedName("x1")
    val x1: Double,
    
    @SerializedName("y1")
    val y1: Double,
    
    @SerializedName("x2")
    val x2: Double,
    
    @SerializedName("y2")
    val y2: Double
)

@Keep
data class LegacyCandidateItemDto(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("img_url")
    val imgUrl: String,
    
    @SerializedName("external_sites")
    val externalSites: List<LegacyExternalSiteDto>? = null,
    
    @SerializedName("category")
    val category: String? = null,
    
    @SerializedName("type")
    val type: String,
    
    @SerializedName("score")
    val score: Double
)

@Keep
data class LegacyExternalSiteDto(
    @SerializedName("site")
    val site: String,
    
    @SerializedName("url")
    val url: String
)
