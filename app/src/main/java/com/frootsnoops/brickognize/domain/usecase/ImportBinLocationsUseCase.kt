package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.domain.model.BinLocationsBackup
import com.frootsnoops.brickognize.domain.model.ImportSummary
import com.frootsnoops.brickognize.domain.model.Result
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

/**
 * Use case for importing bin locations from JSON format
 */
class ImportBinLocationsUseCase @Inject constructor(
    private val repository: BinLocationRepository,
    private val partRepository: PartRepository
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    suspend operator fun invoke(jsonString: String, mergeWithExisting: Boolean = true): Result<ImportSummary> {
        return try {
            val backup = json.decodeFromString<BinLocationsBackup>(jsonString)
            
            if (backup.binLocations.isEmpty()) {
                Timber.w("No bin locations in import data")
                return Result.Error(Exception("No data to import"), "No bin locations found in file")
            }
            
            Timber.i("Importing ${backup.binLocations.size} bin locations (version ${backup.version}, merge=$mergeWithExisting)")
            
            // If not merging, we could optionally clear existing bins first
            // For now, we'll always merge to avoid data loss
            
            var importedCount = 0
            val existingBins = repository.getAllBinLocations()
            val labelToBinId = existingBins.associate { it.label.lowercase() to it.id }.toMutableMap()
            
            backup.binLocations.forEach { exportedBin ->
                // Check if bin with same label already exists
                val existingId = labelToBinId[exportedBin.label.lowercase()]
                
                if (existingId == null) {
                    // Create new bin location
                    val newId = repository.createBinLocation(
                        label = exportedBin.label,
                        description = exportedBin.description
                    )
                    labelToBinId[exportedBin.label.lowercase()] = newId
                    importedCount++
                    Timber.d("Imported bin: ${exportedBin.label}")
                } else {
                    Timber.d("Skipped duplicate bin: ${exportedBin.label}")
                }
            }
            
            // Import parts if present (version 2 and above)
            val parts = backup.parts.orEmpty()
            var partsImported = 0
            if (parts.isNotEmpty()) {
                Timber.i("Importing ${parts.size} parts with assignments")
                val entities = parts.map { pe ->
                    val binId = pe.binLabel?.let { labelToBinId[it.lowercase()] }
                    PartEntity(
                        id = pe.id,
                        name = pe.name,
                        type = pe.type,
                        category = pe.category,
                        imgUrl = pe.imgUrl,
                        binLocationId = binId,
                        createdAt = pe.createdAt,
                        updatedAt = pe.updatedAt
                    )
                }
                try {
                    partRepository.upsertParts(entities)
                    partsImported = entities.size
                } catch (e: Exception) {
                    Timber.e(e, "Failed to upsert parts during import")
                }
            }

            Timber.i("Successfully imported $importedCount bin locations and $partsImported parts")
            Result.Success(ImportSummary(binsImported = importedCount, partsImported = partsImported))
        } catch (e: Exception) {
            Timber.e(e, "Failed to import bin locations")
            Result.Error(e, "Failed to import bin locations: ${e.message}")
        }
    }
}
