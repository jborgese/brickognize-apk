package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ExportBinLocationsUseCaseTest {
    
    private lateinit var repository: BinLocationRepository
    private lateinit var partRepository: PartRepository
    private lateinit var useCase: ExportBinLocationsUseCase
    
    @BeforeEach
    fun setup() {
        repository = mockk()
        partRepository = mockk()
        useCase = ExportBinLocationsUseCase(repository, partRepository)
    }
    
    @Test
    fun `export with bins returns success with JSON`() = runTest {
        // Given
        val bins = listOf(
            BinLocation(1, "Bin A", "First bin", 1000L),
            BinLocation(2, "Bin B", null, 2000L)
        )
        coEvery { repository.getAllBinLocations() } returns bins
        coEvery { partRepository.getAllPartEntities() } returns emptyList()
        coEvery { partRepository.getAllPartBinIds() } returns emptyMap()
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result is Result.Success)
        val jsonString = (result as Result.Success).data
        assertTrue(jsonString.contains("Bin A"))
        assertTrue(jsonString.contains("Bin B"))
        assertTrue(jsonString.contains("First bin"))
        coVerify { repository.getAllBinLocations() }
    }
    
    @Test
    fun `export with no bins returns error`() = runTest {
        // Given
        coEvery { repository.getAllBinLocations() } returns emptyList()
        coEvery { partRepository.getAllPartEntities() } returns emptyList()
        coEvery { partRepository.getAllPartBinIds() } returns emptyMap()
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertTrue(error.message?.contains("No bin locations") == true)
    }
    
    @Test
    fun `export handles repository exception`() = runTest {
        // Given
        coEvery { repository.getAllBinLocations() } throws RuntimeException("DB error")
        coEvery { partRepository.getAllPartEntities() } returns emptyList()
        coEvery { partRepository.getAllPartBinIds() } returns emptyMap()
        
        // When
        val result = useCase()
        
        // Then
        assertTrue(result is Result.Error)
        val error = result as Result.Error
        assertEquals("DB error", error.exception.message)
    }
}
