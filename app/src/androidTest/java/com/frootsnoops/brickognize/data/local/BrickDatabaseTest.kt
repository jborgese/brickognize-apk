package com.frootsnoops.brickognize.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.data.local.entity.BinLocationEntity
import com.frootsnoops.brickognize.data.local.entity.PartEntity
import com.frootsnoops.brickognize.data.local.entity.ScanCandidateEntity
import com.frootsnoops.brickognize.data.local.entity.ScanEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BrickDatabaseTest {

    private lateinit var database: BrickDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrickDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun databaseCreation_initializesAllTables() {
        // Verify all DAOs are accessible
        assertThat(database.binLocationDao()).isNotNull()
        assertThat(database.partDao()).isNotNull()
        assertThat(database.scanDao()).isNotNull()
    }

    @Test
    fun foreignKeyConstraint_binToPart_works() = runTest {
        // Given
        val bin = BinLocationEntity(
            label = "A1",
            description = "Test",
            createdAt = System.currentTimeMillis()
        )
        val binId = database.binLocationDao().insertBinLocation(bin)

        // When
        val part = PartEntity(
            id = "p1",
            name = "Test Part",
            type = "part",
            category = null,
            imgUrl = null,
            binLocationId = binId
        )
        database.partDao().upsertPart(part)

        // Then
        val partWithBin = database.partDao().getPartById("p1")
        assertThat(partWithBin).isNotNull()
        assertThat(partWithBin?.binLocationId).isEqualTo(binId)
    }

    @Test
    fun complexQuery_scanWithCandidatesAndParts_works() = runTest {
        // Given
        val scan = ScanEntity(
            timestamp = System.currentTimeMillis(),
            imagePath = "/complex.jpg"
        )
        val part1 = PartEntity(
            id = "c1",
            name = "Part 1",
            type = "part",
            category = null,
            imgUrl = null,
            binLocationId = null
        )
        val part2 = PartEntity(
            id = "c2",
            name = "Part 2",
            type = "part",
            category = null,
            imgUrl = null,
            binLocationId = null
        )

        // When
        database.partDao().upsertPart(part1)
        database.partDao().upsertPart(part2)
        val scanId = database.scanDao().insertScan(scan)
        
        database.scanDao().insertCandidates(
            listOf(
                ScanCandidateEntity(scanId, "c1", 0, 0.9),
                ScanCandidateEntity(scanId, "c2", 1, 0.8)
            )
        )

        // Then
        val scanWithCandidates = database.scanDao().getScanWithCandidates(scanId)
        assertThat(scanWithCandidates).isNotNull()
        assertThat(scanWithCandidates?.candidates).hasSize(2)
        assertThat(scanWithCandidates?.candidates?.get(0)?.itemId).isEqualTo("c1")
        assertThat(scanWithCandidates?.candidates?.get(1)?.itemId).isEqualTo("c2")
    }

    @Test
    fun flowObservability_detectsChanges() = runTest {
        // Given
        val binFlow = database.binLocationDao().getAllBinLocationsFlow()
        
        // Initial state
        var bins = binFlow.first()
        assertThat(bins).isEmpty()

        // When - insert new bin
        val bin = BinLocationEntity(
            label = "O1",
            description = "Observable",
            createdAt = System.currentTimeMillis()
        )
        database.binLocationDao().insertBinLocation(bin)

        // Then - flow should emit new value
        bins = binFlow.first()
        assertThat(bins).hasSize(1)
        assertThat(bins[0].label).isEqualTo("O1")
    }
}
