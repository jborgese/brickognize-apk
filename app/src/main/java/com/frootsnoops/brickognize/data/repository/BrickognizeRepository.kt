package com.frootsnoops.brickognize.data.repository

import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity
import com.frootsnoops.brickognize.data.remote.api.BrickognizeApi
import com.frootsnoops.brickognize.data.remote.dto.FeedbackRequestDto
import com.frootsnoops.brickognize.data.remote.dto.FeedbackResponseDto
import com.frootsnoops.brickognize.data.remote.dto.LegacyCandidateItemDto
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.BrickItem
import com.frootsnoops.brickognize.domain.model.RecognitionResult
import com.frootsnoops.brickognize.domain.model.RecognitionType
import com.frootsnoops.brickognize.domain.model.Result
import com.frootsnoops.brickognize.util.toMultipartBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BrickognizeRepository @Inject constructor(
    private val api: BrickognizeApi,
    private val scanDao: ScanDao,
    private val partDao: PartDao,
    private val binLocationDao: BinLocationDao
) {
    suspend fun recognizeImage(
        imageFile: File,
        recognitionType: RecognitionType,
        saveImageLocally: Boolean = true
    ): Result<RecognitionResult> = withContext(Dispatchers.IO) {
        try {
            Timber.i("Starting image recognition: type=${recognitionType.name}, file=${imageFile.name}")
            
            // Call API (suspend returns DTO directly; non-2xx throws HttpException)
            val multipartBody = imageFile.toMultipartBody("query_image")
            val dto = when (recognitionType) {
                RecognitionType.PARTS -> {
                    Timber.d("Calling predictPart API")
                    api.predictPart(multipartBody)
                }
                RecognitionType.SETS -> {
                    Timber.d("Calling predictSet API")
                    api.predictSet(multipartBody)
                }
                RecognitionType.FIGS -> {
                    Timber.d("Calling predictFig API")
                    api.predictFig(multipartBody)
                }
            }
            
            Timber.d("API response received: ${dto.items.size} items, listingId=${dto.listingId}")
            
            // Save image path (optional, for now just use the original file path)
            val savedImagePath = if (saveImageLocally) imageFile.absolutePath else null
            
            // Upsert parts to database
            val partEntities = dto.items.map { candidateDto ->
                PartEntity(
                    id = candidateDto.id,
                    name = candidateDto.name,
                    type = candidateDto.type,
                    category = candidateDto.category,
                    imgUrl = candidateDto.imgUrl,
                    binLocationId = null, // Will be set later if already exists
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            
            // Upsert parts (preserving existing bin assignments)
            // Use INSERT-or-UPDATE instead of REPLACE to avoid CASCADE-deleting
            // part_bin_assignments rows (REPLACE = DELETE + INSERT, which triggers CASCADE).
            partEntities.forEach { newPart ->
                val inserted = partDao.insertPartIfAbsent(newPart)
                if (inserted == -1L) {
                    // Part already exists – update only non-bin fields
                    Timber.d("Updating existing part: ${newPart.id}, preserving bin assignments")
                    partDao.updatePartFields(
                        id = newPart.id,
                        name = newPart.name,
                        type = newPart.type,
                        category = newPart.category,
                        imgUrl = newPart.imgUrl,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    Timber.d("Inserted new part: ${newPart.id}")
                }
            }
            
            // Create scan entity
            val topItemId = dto.items.firstOrNull()?.id
            val scan = ScanEntity(
                timestamp = System.currentTimeMillis(),
                imagePath = savedImagePath,
                listingId = dto.listingId,
                topItemId = topItemId,
                recognitionType = recognitionType.apiPath
            )
            
            // Create candidate entities
            val candidates = dto.items.mapIndexed { index, item ->
                ScanCandidateEntity(
                    scanId = 0, // Will be set by the DAO
                    itemId = item.id,
                    rank = index,
                    score = item.score
                )
            }
            
            // Save scan with candidates
            Timber.d("Saving scan to database with ${candidates.size} candidates")
            scanDao.insertScanWithCandidates(scan, candidates)
            
            // Build domain model result
            val brickItems = dto.items.map { candidateDto ->
                val partBinLocations = partDao.getBinLocationsForPart(candidateDto.id).map { bin ->
                    BinLocation(bin.id, bin.label, bin.description, bin.createdAt)
                }
                
                BrickItem(
                    id = candidateDto.id,
                    name = candidateDto.name,
                    type = candidateDto.type,
                    category = candidateDto.category,
                    imgUrl = candidateDto.imgUrl,
                    score = candidateDto.score,
                    binLocation = partBinLocations.firstOrNull(),
                    binLocations = partBinLocations
                )
            }
            
            val recognitionResult = RecognitionResult(
                listingId = dto.listingId,
                topCandidate = brickItems.firstOrNull(),
                candidates = brickItems,
                timestamp = System.currentTimeMillis()
            )
            
            Timber.i("Image recognition completed successfully: ${brickItems.size} items")

            // Clean up temp camera image from cache directory
            if (imageFile.absolutePath.contains("/cache/")) {
                val deleted = imageFile.delete()
                Timber.d("Cleaned up temp image: ${imageFile.name}, deleted=$deleted")
            }

            Result.Success(recognitionResult)
        } catch (e: Exception) {
            Timber.e(e, "Failed to recognize image")
            Result.Error(e)
        }
    }

    suspend fun submitFeedback(
        listingId: String,
        itemId: String,
        itemType: String,
        itemRank: Int,
        isPredictionCorrect: Boolean,
        source: String = "external-app"
    ): Result<FeedbackResponseDto> = withContext(Dispatchers.IO) {
        try {
            Timber.i("Submitting feedback: listingId=$listingId, itemId=$itemId, type=$itemType, rank=$itemRank, correct=$isPredictionCorrect")
            val request = FeedbackRequestDto(
                listingId = listingId,
                itemId = itemId,
                itemType = itemType,
                itemRank = itemRank,
                isPredictionCorrect = isPredictionCorrect,
                source = source
            )
            val body = api.sendFeedback(request)
            Timber.i("Feedback submitted: status=${body.status}, message=${body.message}")
            Result.Success(body)
        } catch (e: Exception) {
            Timber.e(e, "Failed to submit feedback")
            Result.Error(e)
        }
    }
}
