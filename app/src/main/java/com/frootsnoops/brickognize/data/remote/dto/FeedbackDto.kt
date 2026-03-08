package com.frootsnoops.brickognize.data.remote.dto

import androidx.annotation.Keep
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FeedbackRequestDto(
    @SerialName("listing_id")
    val listingId: String,

    @SerialName("item_id")
    val itemId: String,

    @SerialName("item_type")
    val itemType: String, // "part", "set", "fig", "sticker"

    @SerialName("item_rank")
    val itemRank: Int,

    @SerialName("is_prediction_correct")
    val isPredictionCorrect: Boolean,

    @SerialName("source")
    val source: String = "external-app"
)

@Keep
@Serializable
data class FeedbackResponseDto(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String? = null
)
