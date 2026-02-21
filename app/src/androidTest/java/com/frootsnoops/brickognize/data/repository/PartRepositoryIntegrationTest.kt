package com.frootsnoops.brickognize.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.data.local.BrickDatabase
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PartRepositoryIntegrationTest {

    private lateinit var database: BrickDatabase
    private lateinit var partRepository: PartRepository

    @Before
    fun setup() {
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrickDatabase::class.java
        ).allowMainThreadQueries().build()

        partRepository = PartRepository(
            database.partDao(),
            database.binLocationDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertPart_andRetrieveById() = runTest {
        // Given
        val part = PartEntity(
            id = "part-123",
            name = "Test Brick",
            type = "part",
            category = "Bricks"
        )

        // When
        partRepository.upsertPart(part)
        val retrieved = partRepository.getPartById("part-123")

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.name).isEqualTo("Test Brick")
        assertThat(retrieved?.type).isEqualTo("part")
    }

    @Test
    fun assignBinToPart_updatesPartBinLocation() = runTest {
        // Given
        val bin = BinLocationEntity(label = "A1", description = "Test bin")
        val binId = database.binLocationDao().insertBinLocation(bin)
        
        val part = PartEntity(
            id = "part-456",
            name = "Test Part",
            type = "part"
        )
        partRepository.upsertPart(part)

        // When
        partRepository.updatePartBinLocation("part-456", binId)
        val updatedPart = partRepository.getPartById("part-456")

        // Then
        assertThat(updatedPart?.binLocation).isNotNull()
        assertThat(updatedPart?.binLocation?.label).isEqualTo("A1")
    }

    @Test
    fun getPartsByBinLocation_returnsCorrectParts() = runTest {
        // Given
        val bin = BinLocationEntity(label = "B2", description = "Second bin")
        val binId = database.binLocationDao().insertBinLocation(bin)
        
        val part1 = PartEntity(id = "p1", name = "Part 1", type = "part", binLocationId = binId)
        val part2 = PartEntity(id = "p2", name = "Part 2", type = "part", binLocationId = binId)
        val part3 = PartEntity(id = "p3", name = "Part 3", type = "part", binLocationId = null)
        
        partRepository.upsertParts(listOf(part1, part2, part3))

        // When
        val partsInBin = partRepository.getPartsByBinLocationFlow(binId).first()

        // Then
        assertThat(partsInBin).hasSize(2)
        assertThat(partsInBin.map { it.name }).containsExactly("Part 1", "Part 2")
    }

    @Test
    fun clearBinAssignment_removesPartFromBin() = runTest {
        // Given
        val bin = BinLocationEntity(label = "C3")
        val binId = database.binLocationDao().insertBinLocation(bin)
        
        val part = PartEntity(
            id = "part-789",
            name = "Test Part",
            type = "part",
            binLocationId = binId
        )
        partRepository.upsertPart(part)

        // When
        partRepository.updatePartBinLocation("part-789", null)
        val updatedPart = partRepository.getPartById("part-789")

        // Then
        assertThat(updatedPart?.binLocation).isNull()
    }

    @Test
    fun upsertPart_updatesExistingPart() = runTest {
        // Given
        val originalPart = PartEntity(
            id = "part-update",
            name = "Original Name",
            type = "part"
        )
        partRepository.upsertPart(originalPart)

        // When
        val updatedPart = originalPart.copy(name = "Updated Name")
        partRepository.upsertPart(updatedPart)
        val retrieved = partRepository.getPartById("part-update")

        // Then
        assertThat(retrieved?.name).isEqualTo("Updated Name")
    }
}
