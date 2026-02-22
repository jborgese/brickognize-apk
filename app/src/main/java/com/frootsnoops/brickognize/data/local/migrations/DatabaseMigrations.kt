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
     * Introduce a part/bin assignment join table to support many-to-many assignments.
     * Existing single-bin assignments from parts.bin_location_id are preserved.
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS part_bin_assignments (
                    part_id TEXT NOT NULL,
                    bin_location_id INTEGER NOT NULL,
                    assigned_at INTEGER NOT NULL,
                    PRIMARY KEY(part_id, bin_location_id),
                    FOREIGN KEY(part_id) REFERENCES parts(id) ON DELETE CASCADE,
                    FOREIGN KEY(bin_location_id) REFERENCES bin_locations(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )

            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_part_bin_assignments_part_id ON part_bin_assignments(part_id)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_part_bin_assignments_bin_location_id ON part_bin_assignments(bin_location_id)"
            )

            // Migrate existing single-bin values into the new join table.
            db.execSQL(
                """
                INSERT OR IGNORE INTO part_bin_assignments (part_id, bin_location_id, assigned_at)
                SELECT id, bin_location_id, updated_at
                FROM parts
                WHERE bin_location_id IS NOT NULL
                """.trimIndent()
            )
        }
    }

    /**
     * All migrations in order.
     * Add new migrations to this array when created.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        MIGRATION_1_2,
    )
}
