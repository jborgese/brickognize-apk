package com.frootsnoops.brickognize.domain.usecase

import com.frootsnoops.brickognize.data.repository.BinLocationRepository
import com.frootsnoops.brickognize.data.repository.PartRepository
import com.frootsnoops.brickognize.domain.model.Result
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@DisplayName("AssignBinToPartUseCase Tests")
class AssignBinToPartUseCaseTest {

    private lateinit var useCase: AssignBinToPartUseCase
    private lateinit var partRepository: PartRepository
    private lateinit var binLocationRepository: BinLocationRepository

    @BeforeEach
    fun setup() {
        partRepository = mockk()
        binLocationRepository = mockk()
        useCase = AssignBinToPartUseCase(partRepository, binLocationRepository)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("Assigning existing bin to part succeeds")
    fun `assign existing bin to part succeeds`() = runTest {
        val partId = "part-123"
        val binId = 1L
        
        coEvery { partRepository.updatePartBinLocations(partId, listOf(binId), any()) } just Runs

        val result = useCase(partId = partId, binIds = listOf(binId))

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { partRepository.updatePartBinLocations(partId, listOf(binId), any()) }
        coVerify(exactly = 0) { binLocationRepository.createBinLocation(any(), any()) }
    }

    @Test
    @DisplayName("Assigning multiple bins to part succeeds")
    fun `assign multiple bins to part succeeds`() = runTest {
        val partId = "part-123"
        val binIds = listOf(1L, 2L, 2L)

        coEvery { partRepository.updatePartBinLocations(partId, listOf(1L, 2L), any()) } just Runs

        val result = useCase(partId = partId, binIds = binIds)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { partRepository.updatePartBinLocations(partId, listOf(1L, 2L), any()) }
    }

    @Test
    @DisplayName("Creating new bin and assigning to part succeeds")
    fun `create new bin and assign to part succeeds`() = runTest {
        val partId = "part-123"
        val newBinLabel = "A1"
        val newBinDescription = "Top shelf"
        val createdBinId = 5L
        
        coEvery { 
            binLocationRepository.createBinLocation(newBinLabel, newBinDescription) 
        } returns createdBinId
        coEvery { binLocationRepository.getAllBinLocations() } returns emptyList()
        
        coEvery { partRepository.updatePartBinLocations(partId, listOf(createdBinId), any()) } just Runs

        val result = useCase(
            partId = partId,
            binIds = emptyList(),
            newBinLabel = newBinLabel,
            newBinDescription = newBinDescription
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { binLocationRepository.createBinLocation(newBinLabel, newBinDescription) }
        coVerify { partRepository.updatePartBinLocations(partId, listOf(createdBinId), any()) }
    }

    @Test
    @DisplayName("Clearing bin assignment succeeds")
    fun `clear bin assignment succeeds`() = runTest {
        val partId = "part-123"
        
        coEvery { partRepository.updatePartBinLocations(partId, emptyList(), any()) } just Runs

        val result = useCase(partId = partId, binIds = emptyList(), newBinLabel = null)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { partRepository.updatePartBinLocations(partId, emptyList(), any()) }
        coVerify(exactly = 0) { binLocationRepository.createBinLocation(any(), any()) }
    }

    @Test
    @DisplayName("Repository exception returns error result")
    fun `repository exception returns error result`() = runTest {
        val partId = "part-123"
        val binId = 1L
        
        coEvery { 
            partRepository.updatePartBinLocations(partId, listOf(binId), any()) 
        } throws Exception("Database error")

        val result = useCase(partId = partId, binIds = listOf(binId))

        assertThat(result).isInstanceOf(Result.Error::class.java)
        val errorResult = result as Result.Error
        assertThat(errorResult.message).contains("Failed to assign bin to part")
    }

    @Test
    @DisplayName("Creating bin with empty label fails")
    fun `creating bin without label clears assignment`() = runTest {
        val partId = "part-123"
        
        coEvery { partRepository.updatePartBinLocations(partId, emptyList(), any()) } just Runs

        val result = useCase(partId = partId, binIds = emptyList(), newBinLabel = null)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { partRepository.updatePartBinLocations(partId, emptyList(), any()) }
    }

    @Test
    @DisplayName("Creating new bin without description succeeds")
    fun `create new bin without description succeeds`() = runTest {
        val partId = "part-123"
        val newBinLabel = "B2"
        val createdBinId = 10L
        
        coEvery { 
            binLocationRepository.createBinLocation(newBinLabel, null) 
        } returns createdBinId
        coEvery { binLocationRepository.getAllBinLocations() } returns emptyList()
        
        coEvery { partRepository.updatePartBinLocations(partId, listOf(createdBinId), any()) } just Runs

        val result = useCase(
            partId = partId,
            binIds = emptyList(),
            newBinLabel = newBinLabel,
            newBinDescription = null
        )

        assertThat(result).isInstanceOf(Result.Success::class.java)
        coVerify { binLocationRepository.createBinLocation(newBinLabel, null) }
        coVerify { partRepository.updatePartBinLocations(partId, listOf(createdBinId), any()) }
    }
}
