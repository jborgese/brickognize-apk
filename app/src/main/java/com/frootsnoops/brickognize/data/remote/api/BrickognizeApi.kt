package com.frootsnoops.brickognize.data.remote.api

import androidx.annotation.Keep
import com.frootsnoops.brickognize.data.remote.dto.FeedbackRequestDto
import com.frootsnoops.brickognize.data.remote.dto.FeedbackResponseDto
import com.frootsnoops.brickognize.data.remote.dto.LegacySearchResultsDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

@Keep
interface BrickognizeApi {
    
    @Multipart
    @POST("predict/parts/")
    suspend fun predictPart(
        @Part queryImage: MultipartBody.Part
    ): LegacySearchResultsDto
    
    @Multipart
    @POST("predict/sets/")
    suspend fun predictSet(
        @Part queryImage: MultipartBody.Part
    ): LegacySearchResultsDto
    
    @Multipart
    @POST("predict/figs/")
    suspend fun predictFig(
        @Part queryImage: MultipartBody.Part
    ): LegacySearchResultsDto
    
    @POST("feedback/")
    suspend fun sendFeedback(
        @Body feedback: FeedbackRequestDto
    ): FeedbackResponseDto
}
