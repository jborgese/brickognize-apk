package com.frootsnoops.brickognize.data.repository

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.domain.model.BinLocation
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.DisplayName

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("BinLocationRepository Tests")
class BinLocationRepositoryTest {

    private lateinit var repository: BinLocationRepository
    private lateinit var binLocationDao: BinLocationDao

    private val entity1 = BinLocationEntity(1L, "A1", "Top shelf", System.currentTimeMillis())
    private val entity2 = BinLocationEntity(2L, "B2", "Bottom drawer", System.currentTimeMillis())
    
    private val domain1 = BinLocation(1L, "A1", "Top shelf", entity1.createdAt)
    private val domain2 = BinLocation(2L, "B2", "Bottom drawer", entity2.createdAt)

    @BeforeEach
    fun setup() {
        binLocationDao = mockk()
        repository = BinLocationRepository(binLocationDao)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("getAllBinLocationsFlow returns mapped domain models")
    fun `getAllBinLocationsFlow returns domain models`() = runTest {
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(listOf(entity1, entity2))

        repository.getAllBinLocationsFlow().test {
            val result = awaitItem()
            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo(1L)
            assertThat(result[0].label).isEqualTo("A1")
            assertThat(result[1].id).isEqualTo(2L)
            assertThat(result[1].label).isEqualTo("B2")
            awaitComplete()
        }

        verify { binLocationDao.getAllBinLocationsFlow() }
    }

    @Test
    @DisplayName("getAllBinLocationsFlow returns empty list")
    fun `getAllBinLocationsFlow returns empty list`() = runTest {
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(emptyList())

        repository.getAllBinLocationsFlow().test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getAllBinLocations returns mapped domain models")
    fun `getAllBinLocations returns domain models`() = runTest {
        coEvery { binLocationDao.getAllBinLocations() } returns listOf(entity1, entity2)

        val result = repository.getAllBinLocations()

        assertThat(result).hasSize(2)
        assertThat(result[0].label).isEqualTo("A1")
        assertThat(result[1].label).isEqualTo("B2")

        coVerify { binLocationDao.getAllBinLocations() }
    }

    @Test
    @DisplayName("getBinLocationById returns domain model when found")
    fun `getBinLocationById returns domain model when found`() = runTest {
        coEvery { binLocationDao.getBinLocationById(1L) } returns entity1

        val result = repository.getBinLocationById(1L)

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo(1L)
        assertThat(result?.label).isEqualTo("A1")
        assertThat(result?.description).isEqualTo("Top shelf")

        coVerify { binLocationDao.getBinLocationById(1L) }
    }

    @Test
    @DisplayName("getBinLocationById returns null when not found")
    fun `getBinLocationById returns null when not found`() = runTest {
        coEvery { binLocationDao.getBinLocationById(999L) } returns null

        val result = repository.getBinLocationById(999L)

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("createBinLocation inserts entity and returns ID")
    fun `createBinLocation inserts entity and returns ID`() = runTest {
        val label = "C3"
        val description = "Middle rack"
        val slot = slot<BinLocationEntity>()
        
        coEvery { binLocationDao.insertBinLocation(capture(slot)) } returns 3L

        val result = repository.createBinLocation(label, description)

        assertThat(result).isEqualTo(3L)
        assertThat(slot.captured.label).isEqualTo(label)
        assertThat(slot.captured.description).isEqualTo(description)
        assertThat(slot.captured.id).isEqualTo(0L) // Default value before insertion

        coVerify { binLocationDao.insertBinLocation(any()) }
    }

    @Test
    @DisplayName("createBinLocation with null description")
    fun `createBinLocation with null description`() = runTest {
        val label = "D4"
        val slot = slot<BinLocationEntity>()
        
        coEvery { binLocationDao.insertBinLocation(capture(slot)) } returns 4L

        val result = repository.createBinLocation(label, null)

        assertThat(result).isEqualTo(4L)
        assertThat(slot.captured.label).isEqualTo(label)
        assertThat(slot.captured.description).isNull()
    }

    @Test
    @DisplayName("updateBinLocation updates entity in database")
    fun `updateBinLocation updates entity`() = runTest {
        val binLocation = BinLocation(1L, "A1-Updated", "New description", System.currentTimeMillis())
        val slot = slot<BinLocationEntity>()
        
        coEvery { binLocationDao.updateBinLocation(capture(slot)) } just Runs

        repository.updateBinLocation(binLocation)

        assertThat(slot.captured.id).isEqualTo(1L)
        assertThat(slot.captured.label).isEqualTo("A1-Updated")
        assertThat(slot.captured.description).isEqualTo("New description")

        coVerify { binLocationDao.updateBinLocation(any()) }
    }

    @Test
    @DisplayName("deleteBinLocation deletes entity from database")
    fun `deleteBinLocation deletes entity`() = runTest {
        val binLocation = BinLocation(1L, "A1", "Top shelf", System.currentTimeMillis())
        val slot = slot<BinLocationEntity>()
        
        coEvery { binLocationDao.deleteBinLocation(capture(slot)) } just Runs

        repository.deleteBinLocation(binLocation)

        assertThat(slot.captured.id).isEqualTo(1L)
        assertThat(slot.captured.label).isEqualTo("A1")

        coVerify { binLocationDao.deleteBinLocation(any()) }
    }

    @Test
    @DisplayName("getPartCountForBin returns count from DAO")
    fun `getPartCountForBin returns count`() = runTest {
        coEvery { binLocationDao.getPartCountForBin(1L) } returns 5

        val result = repository.getPartCountForBin(1L)

        assertThat(result).isEqualTo(5)

        coVerify { binLocationDao.getPartCountForBin(1L) }
    }

    @Test
    @DisplayName("getPartCountForBin returns zero for empty bin")
    fun `getPartCountForBin returns zero for empty bin`() = runTest {
        coEvery { binLocationDao.getPartCountForBin(2L) } returns 0

        val result = repository.getPartCountForBin(2L)

        assertThat(result).isEqualTo(0)
    }

    @Test
    @DisplayName("Flow emits updates when bins change")
    fun `flow emits updates when bins change`() = runTest {
        val entity3 = BinLocationEntity(3L, "C3", "New bin", System.currentTimeMillis())
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(
            listOf(entity1, entity2),
            listOf(entity1, entity2, entity3)
        )

        repository.getAllBinLocationsFlow().test {
            val first = awaitItem()
            assertThat(first).hasSize(2)

            val second = awaitItem()
            assertThat(second).hasSize(3)
            assertThat(second[2].label).isEqualTo("C3")

            awaitComplete()
        }
    }
}
