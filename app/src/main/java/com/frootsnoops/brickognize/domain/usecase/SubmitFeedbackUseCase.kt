package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BrickognizeRepository
import com.frootsnoops.brickognize.data.remote.dto.FeedbackResponseDto
import com.frootsnoops.brickognize.domain.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class SubmitFeedbackUseCase @Inject constructor(
    private val repository: BrickognizeRepository
) {
    suspend operator fun invoke(
        listingId: String,
        itemId: String,
        itemType: String,
        itemRank: Int,
        isPredictionCorrect: Boolean,
        source: String = "external-app"
    ): Result<FeedbackResponseDto> = withContext(Dispatchers.IO) {
        try {
            Timber.d("SubmitFeedbackUseCase: listingId=$listingId, itemId=$itemId, correct=$isPredictionCorrect")
            repository.submitFeedback(listingId, itemId, itemType, itemRank, isPredictionCorrect, source)
        } catch (e: Exception) {
            Timber.e(e, "SubmitFeedbackUseCase: failed")
            Result.Error(e, e.message ?: "Failed to submit feedback")
        }
    }
}
