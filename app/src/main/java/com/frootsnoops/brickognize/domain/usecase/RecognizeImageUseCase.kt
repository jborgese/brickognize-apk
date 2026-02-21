package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BrickognizeRepository
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.RecognitionType
import com.frootsnoops.brickognize.domain.model.Result
import timber.log.Timber
import java.io.File
import javax.inject.Inject

/**
 * Use case for recognizing LEGO items from an image.
 * 
 * This use case:
 * 1. Sends the image to the Brickognize API
 * 2. Persists the scan and candidates to the local database
 * 3. Upserts recognized parts/sets/figs as PartEntity
 * 4. Returns a RecognitionResult with bin location info where available
 */
class RecognizeImageUseCase @Inject constructor(
    private val repository: BrickognizeRepository
) {
    suspend operator fun invoke(
        imageFile: File,
        recognitionType: RecognitionType,
        saveImageLocally: Boolean = true
    ): Result<RecognitionResult> {
        return try {
            Timber.d("RecognizeImageUseCase: starting recognition for ${imageFile.name}, type=$recognitionType")
            repository.recognizeImage(imageFile, recognitionType, saveImageLocally)
        } catch (e: Exception) {
            Timber.e(e, "RecognizeImageUseCase: exception occurred")
            Result.Error(e, "Failed to recognize image: ${e.message}")
        }
    }
}
