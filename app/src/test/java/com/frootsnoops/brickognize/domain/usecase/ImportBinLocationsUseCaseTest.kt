package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ImportBinLocationsUseCaseTest {
    
    private lateinit var repository: BinLocationRepository
    private lateinit var partRepository: PartRepository
    private lateinit var useCase: ImportBinLocationsUseCase
    
    @BeforeEach
    fun setup() {
        repository = mockk()
        partRepository = mockk()
        useCase = ImportBinLocationsUseCase(repository, partRepository)
    }
    
    @Test
    fun `import with valid JSON creates new bins`() = runTest {
        // Given
        val jsonString = """
            {
                "version": 1,
                "exportedAt": 1234567890,
                "binLocations": [
                    {
                        "label": "New Bin",
                        "description": "Test bin",
                        "createdAt": 1000
                    }
                ]
            }
        """.trimIndent()
        
        coEvery { repository.getAllBinLocations() } returns emptyList()
        coEvery { repository.createBinLocation(any(), any()) } returns 1L
        
        // When
        val result = useCase(jsonString)
        
        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(1, summary.binsImported)
        coVerify { repository.createBinLocation("New Bin", "Test bin") }
    }
    
    @Test
    fun `import skips duplicate bins`() = runTest {
        // Given
        val jsonString = """
            {
                "version": 1,
                "exportedAt": 1234567890,
                "binLocations": [
                    {
                        "label": "Existing Bin",
                        "description": "Test",
                        "createdAt": 1000
                    }
                ]
            }
        """.trimIndent()
        
        val existingBin = BinLocation(1, "Existing Bin", "Old description", 500L)
        coEvery { repository.getAllBinLocations() } returns listOf(existingBin)
        
        // When
        val result = useCase(jsonString)
        
        // Then
        assertTrue(result is Result.Success)
        val summary = (result as Result.Success).data
        assertEquals(0, summary.binsImported) // 0 imported (skipped)
        coVerify(exactly = 0) { repository.createBinLocation(any(), any()) }
    }
    
    @Test
    fun `import with empty data returns error`() = runTest {
        // Given
        val jsonString = """
            {
                "version": 1,
                "exportedAt": 1234567890,
                "binLocations": []
            }
        """.trimIndent()
        
        // When
        val result = useCase(jsonString)
        
        // Then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(error.message?.contains("No bin locations") == true)
    }
    
    @Test
    fun `import with invalid JSON returns error`() = runTest {
        // Given
        val jsonString = "{ invalid json }"
        
        // When
        val result = useCase(jsonString)
        
        // Then
        assertTrue(result is Result.Error)
    }
}
