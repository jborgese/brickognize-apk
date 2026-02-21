package com.frootsnoops.brickognize.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Centralized location for all Room database migrations.
 * 
 * When adding a new migration:
 * 1. Increment version in BrickDatabase
 * 2. Create new MIGRATION_X_Y object here
 * 3. Add migration to DatabaseModule
 * 4. Write migration test in DatabaseMigrationsTest
 * 5. Update schema export (build project)
 * 6. Document changes in MIGRATION_GUIDE.md
 */
object DatabaseMigrations {
    
    /**
     * Example migration from version 1 to 2
     * 
     * Uncomment and modify when you need to add features like:
     * - Adding a new column to an existing table
     * - Creating a new table
     * - Adding an index
     * 
     * Example use case: Adding a 'color' field to bin_locations
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add a color field to bins for visual organization
            database.execSQL(
                """
                ALTER TABLE bin_locations 
                ADD COLUMN color TEXT DEFAULT '#2196F3' NOT NULL
                """.trimIndent()
            )
        }
    }
    
    /**
     * Example migration from version 2 to 3
     * 
     * Example use case: Adding a tags system for parts
     */
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Create a tags table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS tags (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    color TEXT NOT NULL,
                    created_at INTEGER NOT NULL
                )
                """.trimIndent()
            )
            
            // Example: Create a many-to-many join table
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS part_tags (
                    part_id TEXT NOT NULL,
                    tag_id INTEGER NOT NULL,
                    PRIMARY KEY(part_id, tag_id),
                    FOREIGN KEY(part_id) REFERENCES parts(id) ON DELETE CASCADE,
                    FOREIGN KEY(tag_id) REFERENCES tags(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            
            // Create indices for better query performance
            database.execSQL("CREATE INDEX IF NOT EXISTS index_part_tags_part_id ON part_tags(part_id)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_part_tags_tag_id ON part_tags(tag_id)")
        }
    }
    
    /**
     * Example migration from version 3 to 4
     * 
     * Example use case: Adding notes to parts
     */
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Example: Add notes field to parts
            database.execSQL(
                """
                ALTER TABLE parts 
                ADD COLUMN notes TEXT DEFAULT NULL
                """.trimIndent()
            )
        }
    }
    
    /**
     * All migrations in order.
     * Add new migrations to this array when created.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Uncomment as you implement each migration
        // MIGRATION_1_2,
        // MIGRATION_2_3,
        // MIGRATION_3_4,
    )
}
