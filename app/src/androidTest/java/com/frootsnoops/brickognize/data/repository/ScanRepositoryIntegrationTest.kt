package com.frootsnoops.brickognize.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.frootsnoops.brickognize.data.local.BrickDatabase
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
class ScanRepositoryIntegrationTest {

    private lateinit var database: BrickDatabase
    private lateinit var scanRepository: ScanRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            BrickDatabase::class.java
        ).allowMainThreadQueries().build()

        scanRepository = ScanRepository(
            database.scanDao(),
            database.partDao(),
            database.binLocationDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertScan_withCandidates_retrievesCorrectly() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val scan = ScanEntity(
            timestamp = now,
            imagePath = "/path/to/image.jpg"
        )
        val part = PartEntity(
            id = "part-1",
            name = "Test Brick",
            type = "part",
            category = null,
            imgUrl = null,
            binLocationId = null
        )

        // When
        database.partDao().upsertPart(part)
        val scanId = database.scanDao().insertScan(scan)
        database.scanDao().insertCandidates(
            listOf(
                ScanCandidateEntity(
                    scanId = scanId,
                    itemId = "part-1",
                    rank = 0,
                    score = 0.95
                )
            )
        )

        val history = scanRepository.getScanHistoryFlow(50).first()

        // Then
        assertThat(history).hasSize(1)
        assertThat(history[0].topItem?.name).isEqualTo("Test Brick")
        assertThat(history[0].topItem?.score).isWithin(0.01).of(0.95)
    }

    @Test
    fun getAllScans_returnsInReverseChronologicalOrder() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val scan1 = ScanEntity(timestamp = now - 3600000, imagePath = "/scan1.jpg") // 1 hour ago
        val scan2 = ScanEntity(timestamp = now - 1800000, imagePath = "/scan2.jpg") // 30 min ago
        val scan3 = ScanEntity(timestamp = now, imagePath = "/scan3.jpg") // now

        // When
        database.scanDao().insertScan(scan1)
        database.scanDao().insertScan(scan2)
        database.scanDao().insertScan(scan3)

        val scans = scanRepository.getScanHistoryFlow(50).first()

        // Then
        assertThat(scans).hasSize(3)
        // Most recent first
        assertThat(scans[0].imagePath).isEqualTo("/scan3.jpg")
        assertThat(scans[2].imagePath).isEqualTo("/scan1.jpg")
    }

    @Test
    fun emptyScans_returnsEmptyList() = runTest {
        // When
        val scans = scanRepository.getScanHistoryFlow(50).first()

        // Then
        assertThat(scans).isEmpty()
    }
}
