package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BinLocationExport
import com.frootsnoops.brickognize.domain.model.BinLocationsBackup
import com.frootsnoops.brickognize.domain.model.PartExport
import com.frootsnoops.brickognize.domain.model.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for exporting bin locations to JSON format
 */
class ExportBinLocationsUseCase @Inject constructor(
    private val repository: BinLocationRepository,
    private val partRepository: PartRepository
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    suspend operator fun invoke(): Result<String> {
        return try {
            val binLocations = repository.getAllBinLocations()
            
            if (binLocations.isEmpty()) {
                Timber.w("No bin locations to export")
                return Result.Error(Exception("No bin locations found"), "No bin locations to export")
            }
            val binIdToLabel = binLocations.associate { it.id to it.label }
            val partEntities = try {
                partRepository.getAllPartEntities()
            } catch (e: Exception) {
                Timber.e(e, "Failed to load parts for export")
                emptyList()
            }
            
            val exportData = BinLocationsBackup(
                version = 2,
                exportedAt = System.currentTimeMillis(),
                binLocations = binLocations.map { bin ->
                    BinLocationExport(
                        label = bin.label,
                        description = bin.description,
                        createdAt = bin.createdAt
                    )
                },
                parts = partEntities.map { p ->
                    PartExport(
                        id = p.id,
                        name = p.name,
                        type = p.type,
                        category = p.category,
                        imgUrl = p.imgUrl,
                        binLabel = p.binLocationId?.let { binIdToLabel[it] },
                        createdAt = p.createdAt,
                        updatedAt = p.updatedAt
                    )
                }
            )
            
            val jsonString = json.encodeToString(exportData)
            Timber.i("Successfully exported ${binLocations.size} bin locations")
            Result.Success(jsonString)
        } catch (e: Exception) {
            Timber.e(e, "Failed to export bin locations")
            Result.Error(e, "Failed to export bin locations: ${e.message}")
        }
    }
}
