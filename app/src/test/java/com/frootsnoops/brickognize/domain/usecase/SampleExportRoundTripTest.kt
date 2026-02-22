package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SampleExportRoundTripTest {

    @Test
    fun generate_export_and_round_trip_import() = runTest {
        val binRepoExport = mockk<BinLocationRepository>()
        val partRepoExport = mockk<PartRepository>()

        val bins = listOf(
            BinLocation(1L, "A1", "Top shelf", createdAt = 1111L),
            BinLocation(2L, "B2", null, createdAt = 2222L)
        )
        coEvery { binRepoExport.getAllBinLocations() } returns bins

        val parts = listOf(
            PartEntity(id = "3001", name = "Brick 2x4", type = "part", category = "Bricks", imgUrl = "https://img/3001.png", binLocationId = 1L, createdAt = 1234L, updatedAt = 5678L),
            PartEntity(id = "3023", name = "Plate 1x2", type = "part", category = "Plates", imgUrl = "https://img/3023.png", binLocationId = 1L, createdAt = 2234L, updatedAt = 6678L),
            PartEntity(id = "3002", name = "Brick 2x3", type = "part", category = "Bricks", imgUrl = null, binLocationId = null, createdAt = 3234L, updatedAt = 7678L)
        )
        coEvery { partRepoExport.getAllPartEntities() } returns parts
        coEvery {
            partRepoExport.getAllPartBinIds()
        } returns mapOf(
            "3001" to listOf(1L),
            "3023" to listOf(1L)
        )

        val exportUseCase = ExportBinLocationsUseCase(binRepoExport, partRepoExport)

        val exportResult = exportUseCase()
        assertTrue(exportResult is Result.Success)
        val json = (exportResult as Result.Success).data

        // Write sample to a temp file for optional manual inspection.
        val out = File.createTempFile("sample_backup_", ".json")
        out.writeText(json)
        out.deleteOnExit()

        // Now import into fresh repos
        val binRepoImport = mockk<BinLocationRepository>()
        val partRepoImport = mockk<PartRepository>()

        // Start with no bins; createBinLocation returns new IDs
        coEvery { binRepoImport.getAllBinLocations() } returns emptyList()
        coEvery { binRepoImport.createBinLocation("A1", any()) } returns 100L
        coEvery { binRepoImport.createBinLocation("B2", any()) } returns 200L
        coEvery { partRepoImport.upsertParts(any()) } returns Unit
        coEvery { partRepoImport.updatePartBinLocations(any(), any(), any()) } returns Unit

        val importUseCase = ImportBinLocationsUseCase(binRepoImport, partRepoImport)
        val importResult = importUseCase(json)
        assertTrue(importResult is Result.Success)

        // Verify calls
        coVerify { binRepoImport.createBinLocation("A1", "Top shelf") }
        coVerify { binRepoImport.createBinLocation("B2", null) }
        coVerify { partRepoImport.upsertParts(match { list ->
            val a1Assigned = list.count { it.binLocationId == 100L }
            val unassigned = list.count { it.binLocationId == null }
            a1Assigned == 2 && unassigned == 1 && list.size == 3
        }) }
        coVerify { partRepoImport.updatePartBinLocations("3001", listOf(100L), any()) }
        coVerify { partRepoImport.updatePartBinLocations("3023", listOf(100L), any()) }
        coVerify { partRepoImport.updatePartBinLocations("3002", emptyList(), any()) }
    }
}
