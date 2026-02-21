# Database Migrations

This directory contains all Room database migrations for the Brickognize app.

## 📁 Structure

```
migrations/
├── DatabaseMigrations.kt    # All migration definitions
└── README.md               # This file
```

## 🎯 Purpose

Migrations allow the app to update the database schema without losing user data when:
- Adding new features that require database changes
- Fixing schema issues
- Optimizing database performance (adding indices)

## 🚀 Quick Start

### When You Need a Migration

You need to create a migration when you:
1. Add/remove columns from existing tables
2. Create new tables
3. Rename tables or columns
4. Add/remove indices
5. Modify foreign key relationships

### Steps to Create a Migration

1. **Update entity class** with your schema change
2. **Increment version** in `BrickDatabase.kt`
3. **Uncomment migration** in `DatabaseMigrations.kt` (or create new one)
4. **Update `ALL_MIGRATIONS`** array
5. **Write test** in `DatabaseMigrationsTest.kt`
6. **Build project** to export schema
7. **Test thoroughly** before release

See `/MIGRATION_GUIDE.md` for detailed instructions.

## 📋 Migration Tracking

| Version | From | To | Description | Status |
|---------|------|----|-----------|----|
| 1 | - | 1 | Initial schema | ✅ Released |
| 2 | 1 | 2 | Example: Add color to bins | 📝 Template |
| 3 | 2 | 3 | Example: Add tags system | 📝 Template |

## ⚠️ Important Rules

1. **NEVER modify a released migration** - Create a new one instead
2. **ALWAYS test migrations** with `DatabaseMigrationsTest`
3. **NEVER use `.fallbackToDestructiveMigration()`** in production
4. **ALWAYS provide default values** for new NOT NULL columns
5. **DOCUMENT every migration** in MIGRATION_GUIDE.md

## 🧪 Testing

Run migration tests:

```bash
# All migration tests
./gradlew :app:connectedAndroidTest --tests DatabaseMigrationsTest

# Specific migration
./gradlew :app:connectedAndroidTest --tests DatabaseMigrationsTest.migrate1To2_preservesBinLocationData
```

## 📚 Resources

- [Room Migration Guide](https://developer.android.com/training/data-storage/room/migrating-db-versions)
- [SQLite ALTER TABLE](https://www.sqlite.org/lang_altertable.html)
- Project's MIGRATION_GUIDE.md

## 🐛 Troubleshooting

### "Migration didn't properly handle..."
- Compare schema JSON files in `app/schemas/`
- Ensure SQL matches schema changes exactly

### "Migration not found"
- Check migration is added to `ALL_MIGRATIONS` array
- Verify version numbers match database version

### App crashes on update
- Test with `DatabaseMigrationsTest` first
- Check SQL syntax
- Verify column types match entity definitions

---

**Last Updated:** December 3, 2025  
**Current Version:** 1  
**Maintainer:** See MIGRATION_GUIDE.md
