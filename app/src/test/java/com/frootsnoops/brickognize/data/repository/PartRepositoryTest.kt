package com.frootsnoops.brickognize.data.repository

import app.cash.turbine.test
import com.frootsnoops.brickognize.data.local.dao.BinLocationDao
import com.frootsnoops.brickognize.data.local.dao.PartBinAssignmentRef
import com.frootsnoops.brickognize.data.local.dao.PartDao
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("PartRepository Tests")
class PartRepositoryTest {

    private lateinit var repository: PartRepository
    private lateinit var partDao: PartDao
    private lateinit var binLocationDao: BinLocationDao

    private val timestamp = System.currentTimeMillis()
    private val binEntityA1 = BinLocationEntity(1L, "A1", "Top shelf", timestamp)
    private val binEntityB2 = BinLocationEntity(2L, "B2", "Drawer", timestamp)
    private val partEntity = PartEntity("p1", "Brick 2x4", "part", "Bricks", "url1", 1L, timestamp, timestamp)
    private val partEntityNoBins = PartEntity("p2", "Plate 1x2", "part", "Plates", "url2", null, timestamp, timestamp)

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
    @DisplayName("getPartById returns part with all bins")
    fun `getPartById returns part with all bins`() = runTest {
        coEvery { partDao.getPartById("p1") } returns partEntity
        coEvery { partDao.getBinLocationsForPart("p1") } returns listOf(binEntityA1, binEntityB2)

        val result = repository.getPartById("p1")

        assertThat(result).isNotNull()
        assertThat(result?.binLocation?.id).isEqualTo(1L)
        assertThat(result?.binLocations?.map { it.id }).containsExactly(1L, 2L).inOrder()
    }

    @Test
    @DisplayName("getPartById returns part without bins")
    fun `getPartById returns part without bins`() = runTest {
        coEvery { partDao.getPartById("p2") } returns partEntityNoBins
        coEvery { partDao.getBinLocationsForPart("p2") } returns emptyList()

        val result = repository.getPartById("p2")

        assertThat(result).isNotNull()
        assertThat(result?.binLocation).isNull()
        assertThat(result?.binLocations).isEmpty()
    }

    @Test
    @DisplayName("getPartById returns null when part is missing")
    fun `getPartById returns null when missing`() = runTest {
        coEvery { partDao.getPartById("missing") } returns null

        val result = repository.getPartById("missing")

        assertThat(result).isNull()
        coVerify(exactly = 0) { partDao.getBinLocationsForPart(any()) }
    }

    @Test
    @DisplayName("getPartByIdFlow maps bins from assignment table")
    fun `getPartByIdFlow maps bins from assignments`() = runTest {
        every { partDao.getPartByIdFlow("p1") } returns flowOf(partEntity)
        every { partDao.getBinLocationsForPartFlow("p1") } returns flowOf(listOf(binEntityA1, binEntityB2))

        repository.getPartByIdFlow("p1").test {
            val item = awaitItem()
            assertThat(item).isNotNull()
            assertThat(item?.binLocations?.map { it.label }).containsExactly("A1", "B2").inOrder()
            assertThat(item?.binLocation?.label).isEqualTo("A1")
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getPartsByBinLocationFlow returns parts with all assigned bins")
    fun `getPartsByBinLocationFlow returns parts with all assigned bins`() = runTest {
        every { partDao.getPartsByBinLocationFlow(1L) } returns flowOf(listOf(partEntity))
        every { partDao.getAllPartBinAssignmentsFlow() } returns flowOf(
            listOf(
                PartBinAssignmentRef("p1", 1L),
                PartBinAssignmentRef("p1", 2L)
            )
        )
        every { binLocationDao.getAllBinLocationsFlow() } returns flowOf(listOf(binEntityA1, binEntityB2))

        repository.getPartsByBinLocationFlow(1L).test {
            val parts = awaitItem()
            assertThat(parts).hasSize(1)
            assertThat(parts[0].binLocations.map { it.id }).containsExactly(1L, 2L).inOrder()
            awaitComplete()
        }
    }

    @Test
    @DisplayName("getPartsByBinLocation returns mapped parts")
    fun `getPartsByBinLocation returns mapped parts`() = runTest {
        coEvery { partDao.getPartsByBinLocation(1L) } returns listOf(partEntity)
        coEvery { partDao.getAllPartBinAssignments() } returns listOf(
            PartBinAssignmentRef("p1", 1L),
            PartBinAssignmentRef("p1", 2L)
        )
        coEvery { binLocationDao.getAllBinLocations() } returns listOf(binEntityA1, binEntityB2)

        val parts = repository.getPartsByBinLocation(1L)

        assertThat(parts).hasSize(1)
        assertThat(parts[0].binLocations.map { it.label }).containsExactly("A1", "B2").inOrder()
    }

    @Test
    @DisplayName("updatePartBinLocations delegates to DAO transaction")
    fun `updatePartBinLocations delegates to dao`() = runTest {
        coEvery { partDao.replacePartBinAssignments("p1", listOf(1L, 2L), any()) } just Runs

        repository.updatePartBinLocations("p1", listOf(1L, 2L))

        coVerify { partDao.replacePartBinAssignments("p1", listOf(1L, 2L), any()) }
    }

    @Test
    @DisplayName("updatePartBinLocation wraps single-bin updates")
    fun `updatePartBinLocation wraps single bin updates`() = runTest {
        coEvery { partDao.replacePartBinAssignments("p1", listOf(3L), any()) } just Runs

        repository.updatePartBinLocation("p1", 3L)

        coVerify { partDao.replacePartBinAssignments("p1", listOf(3L), any()) }
    }

    @Test
    @DisplayName("getAllPartBinIds groups assignments by part")
    fun `getAllPartBinIds groups assignments`() = runTest {
        coEvery { partDao.getAllPartBinAssignments() } returns listOf(
            PartBinAssignmentRef("p1", 1L),
            PartBinAssignmentRef("p1", 2L),
            PartBinAssignmentRef("p2", 2L)
        )

        val result = repository.getAllPartBinIds()

        assertThat(result["p1"]).containsExactly(1L, 2L).inOrder()
        assertThat(result["p2"]).containsExactly(2L)
    }

    @Test
    @DisplayName("upsert and delete operations delegate to DAO")
    fun `upsert and delete delegate to dao`() = runTest {
        coEvery { partDao.upsertPart(partEntity) } just Runs
        coEvery { partDao.upsertParts(listOf(partEntity, partEntityNoBins)) } just Runs
        coEvery { partDao.deletePart("p1") } just Runs

        repository.upsertPart(partEntity)
        repository.upsertParts(listOf(partEntity, partEntityNoBins))
        repository.deletePart("p1")

        coVerify { partDao.upsertPart(partEntity) }
        coVerify { partDao.upsertParts(listOf(partEntity, partEntityNoBins)) }
        coVerify { partDao.deletePart("p1") }
    }
}
