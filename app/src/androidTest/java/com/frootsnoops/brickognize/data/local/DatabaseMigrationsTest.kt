package com.frootsnoops.brickognize.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.frootsnoops.brickognize.data.local.migrations.DatabaseMigrations
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Tests for Room database migrations.
 * 
 * These tests ensure that:
 * 1. Migrations execute without errors
 * 2. Data is preserved across migrations
 * 3. New schema matches expected structure
 */
@RunWith(AndroidJUnit4::class)
class DatabaseMigrationsTest {
    
    private val TEST_DB = "migration-test"
    
    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        BrickDatabase::class.java
    )
    
    /**
     * Test migration from version 1 to 2
     * 
     * Uncomment when MIGRATION_1_2 is implemented
     */
    /*
    @Test
    @Throws(IOException::class)
    fun migrate1To2_preservesBinLocationData() {
        // Create v1 database with test data
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data
            execSQL("""
                INSERT INTO bin_locations (label, description, created_at) 
                VALUES ('Bin A', 'Small parts', 1000)
            """)
            execSQL("""
                INSERT INTO bin_locations (label, description, created_at) 
                VALUES ('Bin B', 'Large parts', 2000)
            """)
            close()
        }
        
        // Run migration to v2
        val db = helper.runMigrationsAndValidate(
            TEST_DB, 
            2, 
            true, 
            DatabaseMigrations.MIGRATION_1_2
        )
        
        // Verify data preserved
        db.query("SELECT * FROM bin_locations ORDER BY id").use { cursor ->
            assertThat(cursor.count).isEqualTo(2)
            
            // Check first bin
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("label"))).isEqualTo("Bin A")
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("description"))).isEqualTo("Small parts")
            
            // Verify new column exists with default value
            val colorIndex = cursor.getColumnIndexOrThrow("color")
            assertThat(cursor.getString(colorIndex)).isEqualTo("#2196F3")
            
            // Check second bin
            assertThat(cursor.moveToNext()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("label"))).isEqualTo("Bin B")
        }
        
        db.close()
    }
    */
    
    /**
     * Test migration from version 2 to 3
     * 
     * Uncomment when MIGRATION_2_3 is implemented
     */
    /*
    @Test
    @Throws(IOException::class)
    fun migrate2To3_createsTagsTables() {
        // Create v2 database
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL("""
                INSERT INTO bin_locations (label, description, created_at, color) 
                VALUES ('Bin A', 'Test', 1000, '#FF0000')
            """)
            close()
        }
        
        // Run migration to v3
        val db = helper.runMigrationsAndValidate(
            TEST_DB, 
            3, 
            true, 
            DatabaseMigrations.MIGRATION_2_3
        )
        
        // Verify tags table created
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='tags'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
        
        // Verify part_tags table created
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='part_tags'").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
        
        // Verify indices created
        db.query("""
            SELECT name FROM sqlite_master 
            WHERE type='index' AND name='index_part_tags_part_id'
        """).use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
        }
        
        // Verify old data preserved
        db.query("SELECT * FROM bin_locations").use { cursor ->
            assertThat(cursor.count).isEqualTo(1)
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndexOrThrow("label"))).isEqualTo("Bin A")
        }
        
        db.close()
    }
    */
    
    /**
     * Test migration chain from v1 to latest version
     * 
     * This ensures all migrations work together
     */
    /*
    @Test
    @Throws(IOException::class)
    fun migrateAll_preservesData() {
        // Create v1 database with full test dataset
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert bin
            execSQL("""
                INSERT INTO bin_locations (label, description, created_at) 
                VALUES ('Test Bin', 'Migration test', 1000)
            """)
            
            // Insert part
            execSQL("""
                INSERT INTO parts (id, name, type, created_at, updated_at) 
                VALUES ('3001', 'Brick 2x4', 'part', 2000, 2000)
            """)
            
            // Insert scan
            execSQL("""
                INSERT INTO scans (timestamp, recognition_type) 
                VALUES (3000, 'parts')
            """)
            
            close()
        }
        
        // Run all migrations
        val db = helper.runMigrationsAndValidate(
            TEST_DB,
            3,  // Target version
            true,
            *DatabaseMigrations.ALL_MIGRATIONS  // Spread all migrations
        )
        
        // Verify all data preserved
        db.query("SELECT COUNT(*) as count FROM bin_locations").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(1)
        }
        
        db.query("SELECT COUNT(*) as count FROM parts").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(1)
        }
        
        db.query("SELECT COUNT(*) as count FROM scans").use { cursor ->
            cursor.moveToFirst()
            assertThat(cursor.getInt(0)).isEqualTo(1)
        }
        
        db.close()
    }
    */
    
    /**
     * Test that database can be created from scratch at latest version
     */
    @Test
    @Throws(IOException::class)
    fun createDatabaseFromScratch_success() {
        // Create database at latest version
        helper.createDatabase(TEST_DB, 1).apply {
            // Insert test data to verify schema
            execSQL("""
                INSERT INTO bin_locations (label, created_at) 
                VALUES ('Test', 1000)
            """)
            
            // Query to verify
            query("SELECT * FROM bin_locations").use { cursor ->
                assertThat(cursor.count).isEqualTo(1)
            }
            
            close()
        }
    }
}
