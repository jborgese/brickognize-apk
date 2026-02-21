# Brickognize - Build & Setup Guide

## Quick Start

To build the APK from the command line:

```sh
cd /home/frootlab/Documents/brickognize_apk
chmod +x ./gradlew
./gradlew :app:assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

To install directly to a connected device:

```sh
./gradlew :app:installDebug
```

---

## Project Overview

### Generated Directory Structure

```
brickognize_apk/
├── app/
│   ├── build.gradle.kts          # App module build config
│   ├── proguard-rules.pro        # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/frootsnoops/brickognize/
│           │   ├── data/
│           │   │   ├── local/
│           │   │   │   ├── dao/
│           │   │   │   │   ├── BinLocationDao.kt
│           │   │   │   │   ├── PartDao.kt
│           │   │   │   │   └── ScanDao.kt
│           │   │   │   ├── entity/
│           │   │   │   │   ├── BinLocationEntity.kt
│           │   │   │   │   ├── PartEntity.kt
│           │   │   │   │   ├── ScanCandidateEntity.kt
│           │   │   │   │   └── ScanEntity.kt
│           │   │   │   ├── relation/
│           │   │   │   │   └── ScanWithCandidates.kt
│           │   │   │   └── BrickDatabase.kt
│           │   │   ├── remote/
│           │   │   │   ├── api/
│           │   │   │   │   └── BrickognizeApi.kt
│           │   │   │   └── dto/
│           │   │   │       ├── FeedbackDto.kt
│           │   │   │       └── LegacySearchResultsDto.kt
│           │   │   └── repository/
│           │   │       ├── BinLocationRepository.kt
│           │   │       ├── BrickognizeRepository.kt
│           │   │       ├── PartRepository.kt
│           │   │       └── ScanRepository.kt
│           │   ├── di/
│           │   │   ├── DatabaseModule.kt
│           │   │   ├── NetworkModule.kt
│           │   │   └── RepositoryModule.kt
│           │   ├── domain/
│           │   │   ├── model/
│           │   │   │   ├── BinLocation.kt
│           │   │   │   ├── BrickItem.kt
│           │   │   │   ├── RecognitionResult.kt
│           │   │   │   ├── RecognitionType.kt
│           │   │   │   ├── Result.kt
│           │   │   │   └── ScanHistoryItem.kt
│           │   │   └── usecase/
│           │   │       ├── AssignBinToPartUseCase.kt
│           │   │       ├── GetAllBinLocationsUseCase.kt
│           │   │       ├── GetPartsByBinUseCase.kt
│           │   │       ├── GetScanHistoryUseCase.kt
│           │   │       └── RecognizeImageUseCase.kt
│           │   ├── ui/
│           │   │   ├── bins/
│           │   │   │   ├── BinsScreen.kt
│           │   │   │   └── BinsViewModel.kt
│           │   │   ├── history/
│           │   │   │   ├── HistoryScreen.kt
│           │   │   │   └── HistoryViewModel.kt
│           │   │   ├── home/
│           │   │   │   ├── HomeScreen.kt
│           │   │   │   └── HomeViewModel.kt
│           │   │   ├── navigation/
│           │   │   │   ├── BrickognizeNavGraph.kt
│           │   │   │   └── Screen.kt
│           │   │   ├── results/
│           │   │   │   ├── ResultsScreen.kt
│           │   │   │   └── ResultsViewModel.kt
│           │   │   ├── scan/
│           │   │   │   ├── ScanScreen.kt
│           │   │   │   └── ScanViewModel.kt
│           │   │   └── theme/
│           │   │       ├── Color.kt
│           │   │       ├── Theme.kt
│           │   │       └── Type.kt
│           │   ├── util/
│           │   │   ├── FileExtensions.kt
│           │   │   ├── NetworkHelper.kt
│           │   │   └── TimeExtensions.kt
│           │   ├── BrickognizeApp.kt
│           │   └── MainActivity.kt
│           └── res/
│               ├── values/
│               │   ├── colors.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml/
│                   ├── backup_rules.xml
│                   ├── data_extraction_rules.xml
│                   └── file_paths.xml
├── build.gradle.kts              # Root build config
├── gradle.properties             # Gradle properties
├── settings.gradle.kts           # Gradle settings
├── .gitignore
└── README.md
```

---

## Architecture Overview

### Layers

#### 1. **Data Layer** (`data/`)
   - **local/**: Room database implementation
     - **DAOs**: Database access objects for CRUD operations
     - **Entities**: Room table definitions
     - **Database**: Room database configuration
   - **remote/**: API integration
     - **api/**: Retrofit service interfaces
     - **dto/**: Data transfer objects for API responses
   - **repository/**: Repositories that bridge data sources and domain layer

#### 2. **Domain Layer** (`domain/`)
   - **model/**: Domain models (pure Kotlin, no Android dependencies)
   - **usecase/**: Business logic encapsulated in use cases

#### 3. **Presentation Layer** (`ui/`)
   - **screens/**: Jetpack Compose UI screens
   - **viewmodels/**: State management with ViewModels
   - **navigation/**: Compose navigation setup
   - **theme/**: Material 3 theming

#### 4. **Dependency Injection** (`di/`)
   - Hilt modules for providing dependencies

#### 5. **Utilities** (`util/`)
   - Extension functions and helper classes

---

## Key Classes & Responsibilities

### Data Layer

| Class | Responsibility |
|-------|---------------|
| `BrickDatabase` | Room database singleton containing all DAOs |
| `BinLocationDao` | CRUD operations for bin locations |
| `PartDao` | CRUD operations for LEGO parts with bin assignments |
| `ScanDao` | Operations for scan history with candidate items |
| `BrickognizeApi` | Retrofit API interface for Brickognize endpoints |
| `BrickognizeRepository` | Orchestrates API calls and local database writes |
| `BinLocationRepository` | Manages bin location data |
| `PartRepository` | Manages part data with bin assignments |
| `ScanRepository` | Manages scan history with relations |

### Domain Layer

| Class | Responsibility |
|-------|---------------|
| `RecognizeImageUseCase` | Handles image recognition flow (API → DB → Domain) |
| `AssignBinToPartUseCase` | Assigns/creates bin locations for parts |
| `GetAllBinLocationsUseCase` | Retrieves all bin locations as Flow |
| `GetScanHistoryUseCase` | Retrieves scan history with part info |
| `GetPartsByBinUseCase` | Gets all parts assigned to a specific bin |

### Presentation Layer

| Class | Responsibility |
|-------|---------------|
| `HomeViewModel` | Manages home screen state and recognition type selection |
| `ScanViewModel` | Handles image selection and recognition process |
| `ResultsViewModel` | Manages recognition results and bin assignment UI |
| `HistoryViewModel` | Manages scan history display |
| `BinsViewModel` | Manages bin list and bin details views |

---

## Build Commands

### Debug Builds

```sh
# Build debug APK
./gradlew :app:assembleDebug

# Install debug APK to connected device
./gradlew :app:installDebug

# Build and install in one command
./gradlew :app:installDebug
```

### Release Builds

```sh
# Build release APK (unsigned)
./gradlew :app:assembleRelease

# Build release bundle (for Play Store - not needed for sideloading)
./gradlew :app:bundleRelease
```

### Clean & Rebuild

```sh
# Clean build artifacts
./gradlew clean

# Clean and rebuild
./gradlew clean :app:assembleDebug
```

### List Tasks

```sh
# See all available tasks
./gradlew tasks

# See app-specific tasks
./gradlew :app:tasks
```

---

## Development Workflow

### Making Changes

1. **Edit source files** in `app/src/main/java/com/frootsnoops/brickognize/`
2. **Build the project** to check for compilation errors:
   ```sh
   ./gradlew :app:build
   ```
3. **Install to device** for testing:
   ```sh
   ./gradlew :app:installDebug
   ```

### Adding Dependencies

Edit `app/build.gradle.kts` and add to the `dependencies` block:

```kotlin
dependencies {
    implementation("com.example:library:1.0.0")
}
```

Then sync/rebuild:
```sh
./gradlew :app:build
```

### Database Migrations

When changing Room entities:

1. **Increment version** in `BrickDatabase.kt`:
   ```kotlin
   @Database(..., version = 2)
   ```

2. **Create migration**:
   ```kotlin
   val MIGRATION_1_2 = object : Migration(1, 2) {
       override fun migrate(database: SupportSQLiteDatabase) {
           // SQL migration code
       }
   }
   ```

3. **Add to database builder** in `DatabaseModule.kt`:
   ```kotlin
   .addMigrations(MIGRATION_1_2)
   ```

**Current Status**: Using `.fallbackToDestructiveMigration()` for development. Remove this before production use.

---

## Configuration

### API Configuration

File: `app/src/main/java/com/frootsnoops/brickognize/di/NetworkModule.kt`

```kotlin
private const val BASE_URL = "https://api.brickognize.com/"
```

Change this if using a different API endpoint.

### App Configuration

File: `app/build.gradle.kts`

```kotlin
android {
    namespace = "com.frootsnoops.brickognize"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.frootsnoops.brickognize"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }
}
```

### Logging

Debug builds include OkHttp logging interceptor for API calls.

To disable, edit `NetworkModule.kt`:
```kotlin
if (BuildConfig.DEBUG) {
    // Comment out these lines to disable logging
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    builder.addInterceptor(loggingInterceptor)
}
```

---

## Troubleshooting

### Build Fails

1. **Clean the project**:
   ```sh
   ./gradlew clean
   ```

2. **Check JDK version** (should be 17+):
   ```sh
   java -version
   ```

3. **Sync Gradle files** in IDE or:
   ```sh
   ./gradlew --refresh-dependencies
   ```

### APK Won't Install

1. **Check device API level** (minimum is 26 / Android 8.0)
2. **Enable "Install from Unknown Sources"** in device settings
3. **Uninstall previous version** if package name changed

### Database Issues

If you see Room errors:
1. Database schema changed but version not incremented
2. Clear app data on device: Settings → Apps → Brickognize → Clear Data
3. Or increment version and add migration (see above)

### Network Errors

1. **Check internet connection**
2. **Verify API URL** in `NetworkModule.kt`
3. **Check logcat** for detailed error messages:
   ```sh
   adb logcat | grep Brickognize
   ```

---

## Testing

### Manual Testing Checklist

- [ ] Home screen loads successfully
- [ ] Can navigate to all screens
- [ ] Image picker opens when tapping "Select Image"
- [ ] Recognition flow works with valid image
- [ ] Can create new bin location
- [ ] Can assign bin to a part
- [ ] History screen shows scans
- [ ] Bins screen shows created bins
- [ ] Bin details show assigned parts

### Logcat Monitoring

View app logs while running:
```sh
adb logcat | grep -i brickognize
```

View all logs:
```sh
adb logcat
```

Clear logcat:
```sh
adb logcat -c
```

---

## Next Steps

1. **Build the APK**: `./gradlew :app:assembleDebug`
2. **Transfer to device**: `adb install app/build/outputs/apk/debug/app-debug.apk`
3. **Test the app** with real LEGO items
4. **Iterate**: Make changes, rebuild, reinstall

### Future Enhancements

See `README.md` for a full TODO list, including:
- Full CameraX integration
- Export/import functionality
- Feedback submission
- Search and filtering
- Unit tests
- Better error handling

---

## Support

This is a personal project. For issues with the Brickognize API, refer to their documentation at https://api.brickognize.com/

---

**Happy building! 🧱**
