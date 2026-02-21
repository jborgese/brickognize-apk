package com.frootsnoops.brickognize.data.remote.dto

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class FeedbackRequestDto(
    @SerializedName("listing_id")
    val listingId: String,
    
    @SerializedName("item_id")
    val itemId: String,
    
    @SerializedName("item_type")
    val itemType: String, // "part", "set", "fig", "sticker"
    
    @SerializedName("item_rank")
    val itemRank: Int,
    
    @SerializedName("is_prediction_correct")
    val isPredictionCorrect: Boolean,
    
    @SerializedName("source")
    val source: String = "external-app"
)

@Keep
data class FeedbackResponseDto(
    @SerializedName("status")
    val status: String,
    
    @SerializedName("message")
    val message: String? = null
)
