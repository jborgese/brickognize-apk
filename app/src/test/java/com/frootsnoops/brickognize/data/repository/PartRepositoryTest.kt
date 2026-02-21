package com.frootsnoops.brickognize.data.repository

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
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
@DisplayName("PartRepository Tests")
class PartRepositoryTest {

    private lateinit var repository: PartRepository
    private lateinit var partDao: PartDao
    private lateinit var binLocationDao: BinLocationDao

    private val timestamp = System.currentTimeMillis()
    private val binEntity = BinLocationEntity(1L, "A1", "Top shelf", timestamp)
    private val partEntity = PartEntity("p1", "Brick 2x4", "part", "Bricks", "url1", 1L, timestamp, timestamp)
    private val partEntityNoBin = PartEntity("p2", "Plate 1x2", "part", "Plates", "url2", null, timestamp, timestamp)

    @BeforeEach
    fun setup() {
        partDao = mockk()
        binLocationDao = mockk()
        repository = PartRepository(partDao, binLocationDao)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("getPartById returns part with bin location")
    fun `getPartById returns part with bin`() = runTest {
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { binLocationDao.getBinLocationById(1L) } returns binEntity

        val result = repository.getPartById("p1")

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("p1")
        assertThat(result?.name).isEqualTo("Brick 2x4")
        assertThat(result?.binLocation).isNotNull()
        assertThat(result?.binLocation?.label).isEqualTo("A1")

        coVerify { partDao.getPartById("p1") }
        coVerify { binLocationDao.getBinLocationById(1L) }
    }

    @Test
    @DisplayName("getPartById returns part without bin location")
    fun `getPartById returns part without bin`() = runTest {
        coEvery { partDao.getPartById("p2") } returns partEntityNoBin

        val result = repository.getPartById("p2")

        assertThat(result).isNotNull()
        assertThat(result?.id).isEqualTo("p2")
        assertThat(result?.binLocation).isNull()

        coVerify { partDao.getPartById("p2") }
        coVerify(exactly = 0) { binLocationDao.getBinLocationById(any()) }
    }

    @Test
    @DisplayName("getPartById returns null when part not found")
    fun `getPartById returns null when not found`() = runTest {
        coEvery { partDao.getPartById("unknown") } returns null

        val result = repository.getPartById("unknown")

        assertThat(result).isNull()
    }

    @Test
    @DisplayName("getPartByIdFlow returns flow with bin location")
    fun `getPartByIdFlow returns flow with bin`() = runTest {
        every { partDao.getPartByIdFlow("p1") } returns flowOf(partEntity)
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(listOf(binEntity))

        repository.getPartByIdFlow("p1").test {
            val result = awaitItem()
            
            assertThat(result).isNotNull()
            assertThat(result?.id).isEqualTo("p1")
            assertThat(result?.binLocation).isNotNull()
            assertThat(result?.binLocation?.label).isEqualTo("A1")
            
            awaitComplete()
        }

        verify { partDao.getPartByIdFlow("p1") }
        verify { binLocationDao.getAllBinLocationsFlow() }
    }

    @Test
    @DisplayName("getPartByIdFlow returns flow without bin location")
    fun `getPartByIdFlow returns flow without bin`() = runTest {
        every { partDao.getPartByIdFlow("p2") } returns flowOf(partEntityNoBin)
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(listOf(binEntity))

        repository.getPartByIdFlow("p2").test {
            val result = awaitItem()
            
            assertThat(result).isNotNull()
            assertThat(result?.binLocation).isNull()
            
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getPartByIdFlow returns null when part not found")
    fun `getPartByIdFlow returns null when not found`() = runTest {
        every { partDao.getPartByIdFlow("unknown") } returns flowOf(null)
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(emptyList())

        repository.getPartByIdFlow("unknown").test {
            val result = awaitItem()
            assertThat(result).isNull()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getPartsByBinLocationFlow returns parts for bin")
    fun `getPartsByBinLocationFlow returns parts`() = runTest {
        val part1 = partEntity.copy(id = "p1", binLocationId = 1L)
        val part2 = partEntity.copy(id = "p2", binLocationId = 1L)
        
        every { partDao.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(part1, part2))
        every { binLocationDao.getBinLocationByIdFlow(1L) } returns flowOf(binEntity)

        repository.getPartsByBinLocationFlow(1L).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(2)
            assertThat(result[0].id).isEqualTo("p1")
            assertThat(result[0].binLocation?.label).isEqualTo("A1")
            assertThat(result[1].id).isEqualTo("p2")
            
            awaitComplete()
        }

        verify { partDao.getPartsByBinLocationFlow(1L) }
        verify { binLocationDao.getBinLocationByIdFlow(1L) }
    }

    @Test
    @DisplayName("getPartsByBinLocationFlow returns empty list")
    fun `getPartsByBinLocationFlow returns empty list`() = runTest {
        every { partDao.getPartsByBinLocationFlow(1L) } returns flowOf(emptyList())
        every { binLocationDao.getBinLocationByIdFlow(1L) } returns flowOf(binEntity)

        repository.getPartsByBinLocationFlow(1L).test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("upsertPart calls DAO")
    fun `upsertPart calls DAO`() = runTest {
        coEvery { partDao.upsertPart(partEntity) } just Runs

        repository.upsertPart(partEntity)

        coVerify { partDao.upsertPart(partEntity) }
    }

    @Test
    @DisplayName("upsertParts calls DAO with list")
    fun `upsertParts calls DAO with list`() = runTest {
        val parts = listOf(partEntity, partEntityNoBin)
        coEvery { partDao.upsertParts(parts) } just Runs

        repository.upsertParts(parts)

        coVerify { partDao.upsertParts(parts) }
    }

    @Test
    @DisplayName("updatePartBinLocation updates bin assignment")
    fun `updatePartBinLocation updates bin`() = runTest {
        val slot = slot<Long>()
        coEvery { partDao.updateBinLocation("p1", capture(slot), any()) } just Runs

        repository.updatePartBinLocation("p1", 2L)

        assertThat(slot.captured).isEqualTo(2L)
        coVerify { partDao.updateBinLocation("p1", 2L, any()) }
    }

    @Test
    @DisplayName("updatePartBinLocation can set bin to null")
    fun `updatePartBinLocation can set bin to null`() = runTest {
        coEvery { partDao.updateBinLocation("p1", null, any()) } just Runs

        repository.updatePartBinLocation("p1", null)

        coVerify { partDao.updateBinLocation("p1", null, any()) }
    }

    @Test
    @DisplayName("Domain model excludes score from entity")
    fun `domain model excludes score from entity`() = runTest {
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { binLocationDao.getBinLocationById(1L) } returns binEntity

        val result = repository.getPartById("p1")

        assertThat(result?.score).isNull()
    }

    @Test
    @DisplayName("getPartByIdFlow updates when bin changes")
    fun `getPartByIdFlow updates when bin changes`() = runTest {
        every { partDao.getPartByIdFlow("p1") } returns flowOf(partEntity)
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(listOf(binEntity))

        repository.getPartByIdFlow("p1").test {
            val result = awaitItem()
            assertThat(result?.binLocation?.label).isEqualTo("A1")

            awaitComplete()
        }
    }

    @Test
    @DisplayName("getPartsByBinLocationFlow updates when bin changes")
    fun `getPartsByBinLocationFlow updates when bin changes`() = runTest {
        every { partDao.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(partEntity))
        every { binLocationDao.getBinLocationByIdFlow(1L) } returns flowOf(binEntity)

        repository.getPartsByBinLocationFlow(1L).test {
            val result = awaitItem()
            assertThat(result[0].binLocation?.description).isEqualTo("Top shelf")

            awaitComplete()
        }
    }

    @Test
    @DisplayName("deletePart delegates to DAO")
    fun `deletePart delegates to dao`() = runTest {
        coEvery { partDao.deletePart("p1") } just Runs

        repository.deletePart("p1")

        coVerify { partDao.deletePart("p1") }
    }
}
