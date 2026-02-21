# Database Migration Guide

## Overview

This guide explains how to safely update the Room database schema without losing user data.

---

## ⚠️ Critical Rule

**NEVER use `.fallbackToDestructiveMigration()` in production builds!**

This deletes all user data on schema changes. Only use it in debug builds during development.

---

## Migration Checklist

When you need to modify the database schema:

### 1. **Update Entity Classes**

Modify your entity class (e.g., add a field):

```kotlin
@Entity(tableName = "bin_locations")
data class BinLocationEntity(
    // ... existing fields ...
    
    @ColumnInfo(name = "color")
    val color: String = "#2196F3"  // Add new field with default value
)
```

### 2. **Increment Database Version**

```kotlin
@Database(
    entities = [...],
    version = 2,  // Changed from 1
    exportSchema = true
)
abstract class BrickDatabase : RoomDatabase() {
    // ...
}
```

### 3. **Create Migration**

In `DatabaseMigrations.kt`, uncomment and modify the appropriate migration:

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE bin_locations ADD COLUMN color TEXT DEFAULT '#2196F3' NOT NULL"
        )
    }
}
```

### 4. **Add Migration to DatabaseModule**

Uncomment the migration in `ALL_MIGRATIONS` array:

```kotlin
val ALL_MIGRATIONS = arrayOf(
    MIGRATION_1_2,  // Uncommented
    // MIGRATION_2_3,
)
```

### 5. **Write Migration Test**

In `DatabaseMigrationsTest.kt`:

```kotlin
@Test
fun migrate1To2_preservesData() = runTest {
    // Create v1 database
    helper.createDatabase(TEST_DB, 1).apply {
        execSQL("INSERT INTO bin_locations (label, description, created_at) VALUES ('Bin A', 'Test', 1000)")
        close()
    }
    
    // Run migration
    helper.runMigrationsAndValidate(TEST_DB, 2, true, DatabaseMigrations.MIGRATION_1_2)
    
    // Verify data preserved and new column exists
    helper.runMigrationsAndValidate(TEST_DB, 2, true).apply {
        query("SELECT * FROM bin_locations").use { cursor ->
            assertThat(cursor.moveToFirst()).isTrue()
            assertThat(cursor.getString(cursor.getColumnIndex("label"))).isEqualTo("Bin A")
            assertThat(cursor.getString(cursor.getColumnIndex("color"))).isEqualTo("#2196F3")
        }
        close()
    }
}
```

### 6. **Build & Export Schema**

```bash
./gradlew :app:assembleDebug
```

This exports the new schema to `app/schemas/com.frootsnoops.brickognize.data.local.BrickDatabase/2.json`

### 7. **Test on Real Device**

```bash
# Install v1
./gradlew :app:installDebug

# Add some data (bins, scans, etc.)

# Install v2
./gradlew :app:installDebug

# Verify data is preserved
```

---

## Common Migration Scenarios

### Adding a Column

```kotlin
database.execSQL("ALTER TABLE table_name ADD COLUMN new_column TEXT DEFAULT 'default_value' NOT NULL")
```

### Creating a New Table

```kotlin
database.execSQL("""
    CREATE TABLE IF NOT EXISTS new_table (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        name TEXT NOT NULL,
        created_at INTEGER NOT NULL
    )
""")
```

### Adding an Index

```kotlin
database.execSQL("CREATE INDEX IF NOT EXISTS index_table_column ON table_name(column_name)")
```

### Renaming a Column (Requires Recreation)

```kotlin
// 1. Create new table with correct schema
database.execSQL("CREATE TABLE new_table (...)")

// 2. Copy data
database.execSQL("INSERT INTO new_table SELECT ... FROM old_table")

// 3. Drop old table
database.execSQL("DROP TABLE old_table")

// 4. Rename new table
database.execSQL("ALTER TABLE new_table RENAME TO old_table")

// 5. Recreate indices
database.execSQL("CREATE INDEX ...")
```

---

## Testing Migrations

### Unit Test (Fast)

```bash
./gradlew :app:testDebugUnitTest --tests DatabaseMigrationsTest
```

### Integration Test (Real Device)

```bash
# Install old version
adb install app/build/outputs/apk/debug/app-debug-v1.apk

# Add test data
adb shell am start -n com.frootsnoops.brickognize/.MainActivity

# Install new version
adb install app/build/outputs/apk/debug/app-debug-v2.apk

# Verify data preserved
adb shell am start -n com.frootsnoops.brickognize/.MainActivity
```

---

## Migration History

### Version 1 → 2 (Not Yet Released)

**Date:** TBD  
**Changes:**
- Example: Added `color` column to `bin_locations` table
- Example: Default color is `#2196F3` (blue)

**Migration:** `MIGRATION_1_2`

### Version 2 → 3 (Future)

**Date:** TBD  
**Changes:**
- Example: Added `tags` table for part organization
- Example: Added `part_tags` join table for many-to-many relationship

**Migration:** `MIGRATION_2_3`

---

## Troubleshooting

### "Migration didn't properly handle..."

**Cause:** Migration SQL doesn't match actual schema changes.

**Fix:** Compare `app/schemas/.../X.json` with `Y.json` and ensure migration SQL recreates the differences.

### "A migration from X to Y was required but not found"

**Cause:** Migration not added to `ALL_MIGRATIONS` array.

**Fix:** Uncomment migration in `DatabaseMigrations.kt` and add to array.

### App crashes on update

**Cause:** Migration has SQL syntax error or references non-existent columns.

**Fix:** Test migration with `DatabaseMigrationsTest` before release.

### Need to migrate data between columns

Use temporary column:

```kotlin
// 1. Add new column
database.execSQL("ALTER TABLE parts ADD COLUMN new_type TEXT")

// 2. Copy data with transformation
database.execSQL("UPDATE parts SET new_type = UPPER(type)")

// 3. Drop old column (requires table recreation, see "Renaming a Column" above)
```

---

## Best Practices

1. ✅ **Always test migrations** before releasing
2. ✅ **Use default values** for new NOT NULL columns
3. ✅ **Keep migrations simple** - one logical change per migration
4. ✅ **Document migrations** in this file
5. ✅ **Never modify old migrations** after release
6. ✅ **Export schema** after each change
7. ✅ **Test on low-end devices** (migrations can be slow on large databases)

---

## Resources

- [Room Migration Documentation](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [SQLite ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
- [Testing Room Migrations](https://developer.android.com/training/data-storage/room/migrating-db-versions#test)

---

**Last Updated:** December 3, 2025  
**Current Database Version:** 1  
**Next Planned Version:** 2 (when needed)
