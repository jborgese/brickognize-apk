package com.frootsnoops.brickognize.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.data.local.BrickDatabase
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

@RunWith(AndroidJUnit4::class)
class BinLocationRepositoryIntegrationTest {

    private lateinit var database: BrickDatabase
    private lateinit var binRepository: BinLocationRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrickDatabase::class.java
        ).allowMainThreadQueries().build()

        binRepository = BinLocationRepository(database.binLocationDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertBin_andRetrieveAll() = runTest {
        // Given
        val bin1 = BinLocationEntity(
            label = "A1",
            description = "First bin",
            createdAt = Instant.now().toEpochMilli()
        )
        val bin2 = BinLocationEntity(
            label = "B2",
            description = "Second bin",
            createdAt = Instant.now().toEpochMilli()
        )

        // When
        database.binLocationDao().insertBinLocation(bin1)
        database.binLocationDao().insertBinLocation(bin2)
        val allBins = binRepository.getAllBinLocationsFlow().first()

        // Then
        assertThat(allBins).hasSize(2)
        assertThat(allBins.map { it.label }).containsAtLeast("A1", "B2")
    }

    @Test
    fun updateBin_modifiesExistingData() = runTest {
        // Given
        val bin = BinLocationEntity(
            label = "C3",
            description = "Original",
            createdAt = Instant.now().toEpochMilli()
        )
        val id = database.binLocationDao().insertBinLocation(bin)

        // When
        val updated = bin.copy(id = id, description = "Updated")
        database.binLocationDao().updateBinLocation(updated)
        val retrieved = binRepository.getBinLocationById(id)

        // Then
        assertThat(retrieved?.description).isEqualTo("Updated")
    }

    @Test
    fun deleteBin_removesFromDatabase() = runTest {
        // Given
        val bin = BinLocationEntity(
            label = "D4",
            description = "To delete",
            createdAt = Instant.now().toEpochMilli()
        )
        val id = database.binLocationDao().insertBinLocation(bin)

        // When
        database.binLocationDao().deleteBinLocation(bin.copy(id = id))
        val retrieved = binRepository.getBinLocationById(id)

        // Then
        assertThat(retrieved).isNull()
    }

    @Test
    fun getAllBinLocations_returnsEmptyWhenNoBins() = runTest {
        // When
        val bins = binRepository.getAllBinLocationsFlow().first()

        // Then
        assertThat(bins).isEmpty()
    }
}
