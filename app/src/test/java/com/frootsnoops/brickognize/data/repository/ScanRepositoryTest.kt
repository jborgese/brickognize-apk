package com.frootsnoops.brickognize.data.repository

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.dao.ScanDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity
import com.frootsnoops.brickognize.data.local.relation.ScanWithCandidates
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
@DisplayName("ScanRepository Tests")
class ScanRepositoryTest {

    private lateinit var repository: ScanRepository
    private lateinit var scanDao: ScanDao
    private lateinit var partDao: PartDao
    private lateinit var binLocationDao: BinLocationDao

    private val timestamp = System.currentTimeMillis()
    private val binEntity = BinLocationEntity(1L, "A1", "Top shelf", timestamp)
    private val partEntity = PartEntity("p1", "Part 1", "part", null, "url1", 1L, timestamp, timestamp)
    private val scanEntity = ScanEntity(
        id = 1L,
        timestamp = timestamp,
        imagePath = "/path/image.jpg",
        topItemId = "p1",
        recognitionType = "parts"
    )
    private val candidateEntity1 = ScanCandidateEntity(1L, "p1", 1, 0.95)
    private val candidateEntity2 = ScanCandidateEntity(1L, "p2", 2, 0.85)

    @BeforeEach
    fun setup() {
        scanDao = mockk()
        partDao = mockk()
        binLocationDao = mockk()
        repository = ScanRepository(scanDao, partDao, binLocationDao)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    @DisplayName("getScanHistoryFlow returns mapped history items")
    fun `getScanHistoryFlow returns history items`() = runTest {
        val scanWithCandidates = ScanWithCandidates(
            scan = scanEntity,
            candidates = listOf(candidateEntity1, candidateEntity2)
        )
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scanWithCandidates))
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { binLocationDao.getBinLocationById(1L) } returns binEntity

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(1)
            val historyItem = result[0]
            assertThat(historyItem.scanId).isEqualTo(1L)
            assertThat(historyItem.recognitionType).isEqualTo("parts")
            assertThat(historyItem.candidateCount).isEqualTo(2)
            assertThat(historyItem.topItem).isNotNull()
            assertThat(historyItem.topItem?.id).isEqualTo("p1")
            assertThat(historyItem.topItem?.name).isEqualTo("Part 1")
            assertThat(historyItem.topItem?.binLocation).isNotNull()
            assertThat(historyItem.topItem?.binLocation?.label).isEqualTo("A1")
            
            awaitComplete()
        }

        verify { scanDao.getRecentScansWithCandidatesFlow(50) }
        coVerify { partDao.getPartById("p1") }
        coVerify { binLocationDao.getBinLocationById(1L) }
    }

    @Test
    @DisplayName("getScanHistoryFlow handles scan without topItem")
    fun `getScanHistoryFlow handles scan without topItem`() = runTest {
        val scanWithoutTopItem = ScanWithCandidates(
            scan = scanEntity.copy(topItemId = null),
            candidates = listOf(candidateEntity1)
        )
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scanWithoutTopItem))

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(1)
            val historyItem = result[0]
            assertThat(historyItem.topItem).isNull()
            assertThat(historyItem.candidateCount).isEqualTo(1)
            
            awaitComplete()
        }

        verify { scanDao.getRecentScansWithCandidatesFlow(50) }
        coVerify(exactly = 0) { partDao.getPartById(any()) }
    }

    @Test
    @DisplayName("getScanHistoryFlow handles part without bin location")
    fun `getScanHistoryFlow handles part without bin`() = runTest {
        val partWithoutBin = partEntity.copy(binLocationId = null)
        val scanWithCandidates = ScanWithCandidates(
            scan = scanEntity,
            candidates = listOf(candidateEntity1)
        )
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scanWithCandidates))
        coEvery { partDao.getPartById("p1") } returns partWithoutBin

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(1)
            val historyItem = result[0]
            assertThat(historyItem.topItem).isNotNull()
            assertThat(historyItem.topItem?.binLocation).isNull()
            
            awaitComplete()
        }

        verify { scanDao.getRecentScansWithCandidatesFlow(50) }
        coVerify { partDao.getPartById("p1") }
        coVerify(exactly = 0) { binLocationDao.getBinLocationById(any()) }
    }

    @Test
    @DisplayName("getScanHistoryFlow handles part not found")
    fun `getScanHistoryFlow handles part not found`() = runTest {
        val scanWithCandidates = ScanWithCandidates(
            scan = scanEntity,
            candidates = listOf(candidateEntity1)
        )
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scanWithCandidates))
        coEvery { partDao.getPartById("p1") } returns null

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(1)
            val historyItem = result[0]
            assertThat(historyItem.topItem).isNull()
            
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getScanHistoryFlow returns empty list")
    fun `getScanHistoryFlow returns empty list`() = runTest {
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(emptyList())

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            assertThat(result).isEmpty()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getScanHistoryFlow uses custom limit")
    fun `getScanHistoryFlow uses custom limit`() = runTest {
        every { scanDao.getRecentScansWithCandidatesFlow(10) } returns flowOf(emptyList())

        repository.getScanHistoryFlow(10).test {
            awaitItem()
            awaitComplete()
        }

        verify { scanDao.getRecentScansWithCandidatesFlow(10) }
    }

    @Test
    @DisplayName("getScanHistoryFlow uses default limit")
    fun `getScanHistoryFlow uses default limit`() = runTest {
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(emptyList())

        repository.getScanHistoryFlow().test {
            awaitItem()
            awaitComplete()
        }

        verify { scanDao.getRecentScansWithCandidatesFlow(50) }
    }

    @Test
    @DisplayName("getScanHistoryFlow handles multiple scans")
    fun `getScanHistoryFlow handles multiple scans`() = runTest {
        val scan1 = ScanWithCandidates(
            scan = scanEntity.copy(id = 1L, topItemId = "p1"),
            candidates = listOf(candidateEntity1)
        )
        val scan2 = ScanWithCandidates(
            scan = scanEntity.copy(id = 2L, topItemId = "p2", recognitionType = "sets"),
            candidates = listOf(candidateEntity2)
        )
        
        val part2 = partEntity.copy(id = "p2", name = "Set 1", type = "set", binLocationId = null)
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scan1, scan2))
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { partDao.getPartById("p2") } returns part2
        coEvery { binLocationDao.getBinLocationById(1L) } returns binEntity

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result).hasSize(2)
            assertThat(result[0].scanId).isEqualTo(1L)
            assertThat(result[0].recognitionType).isEqualTo("parts")
            assertThat(result[1].scanId).isEqualTo(2L)
            assertThat(result[1].recognitionType).isEqualTo("sets")
            
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getScanHistoryFlow preserves candidate count")
    fun `getScanHistoryFlow preserves candidate count`() = runTest {
        val candidates = listOf(
            candidateEntity1,
            candidateEntity2,
            ScanCandidateEntity(1L, "p3", 3, 0.75)
        )
        val scanWithCandidates = ScanWithCandidates(
            scan = scanEntity,
            candidates = candidates
        )
        
        every { scanDao.getRecentScansWithCandidatesFlow(50) } returns flowOf(listOf(scanWithCandidates))
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { binLocationDao.getBinLocationById(1L) } returns binEntity

        repository.getScanHistoryFlow(50).test {
            val result = awaitItem()
            
            assertThat(result[0].candidateCount).isEqualTo(3)
            
            awaitComplete()
        }
    }
}
