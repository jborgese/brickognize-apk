# Brickognize Android App

A personal Android app for recognizing LEGO parts, sets, and minifigures using the Brickognize API. The app lets you scan LEGO items with your phone's camera, view recognition results, and organize parts by assigning them to bin locations.

## Features

- 📸 **Image Recognition**: Capture or select photos to identify LEGO parts, sets, and minifigures
- 🗂️ **Bin Organization**: Assign parts to custom bin locations for easy physical organization
- 📊 **Local Storage**: All data stored locally on device using Room database
- 📜 **Scan History**: View past scans and their results
- 🔌 **Offline-First**: No external backend required (except Brickognize API for recognition)

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM with clean architecture layers
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Coil
- **Camera**: CameraX (skeleton implementation; currently uses image picker)

## Project Structure

```
app/src/main/java/com/frootsnoops/brickognize/
├── data/
│   ├── local/
│   │   ├── dao/              # Room DAOs
│   │   ├── entity/           # Room entities
│   │   ├── relation/         # Room relations
│   │   └── BrickDatabase.kt
│   ├── remote/
│   │   ├── api/              # Retrofit API interface
│   │   └── dto/              # Data transfer objects
│   └── repository/           # Repository implementations
├── di/                       # Hilt dependency injection modules
├── domain/
│   ├── model/                # Domain models
│   └── usecase/              # Business logic use cases
├── ui/
│   ├── bins/                 # Bin management screens
│   ├── history/              # Scan history screen
│   ├── home/                 # Home screen
│   ├── navigation/           # Navigation setup
│   ├── results/              # Recognition results screen
│   ├── scan/                 # Scan/camera screen
│   └── theme/                # Compose theme
├── util/                     # Utility classes and extensions
├── BrickognizeApp.kt         # Application class
└── MainActivity.kt           # Main activity
```

## Building the App

### Prerequisites

- JDK 17 or higher
- Android SDK with API 35 (Android 15)
- Git (optional)

### Build from Command Line

1. **Clone or navigate to the project directory**:
   ```sh
   cd <path-to-repo>
   ```

2. **Build debug APK**:
   ```sh
   # macOS/Linux
   ./gradlew :app:assembleDebug

   # Windows (PowerShell/CMD)
   .\gradlew.bat :app:assembleDebug
   ```
   
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Build release APK** (unsigned):
   ```sh
   # macOS/Linux
   ./gradlew :app:assembleRelease

   # Windows (PowerShell/CMD)
   .\gradlew.bat :app:assembleRelease
   ```

4. **Install directly to connected device**:
   ```sh
   # macOS/Linux
   ./gradlew :app:installDebug

   # Windows (PowerShell/CMD)
   .\gradlew.bat :app:installDebug
   ```

### Build with VS Code

If you have the Android extension for VS Code:

1. Open the project folder in VS Code
2. Use the Command Palette (Ctrl+Shift+P / Cmd+Shift+P)
3. Run: `Android: Build APK`

## Configuration

### API Base URL

The Brickognize API base URL is configured in:
`app/src/main/java/com/frootsnoops/brickognize/di/NetworkModule.kt`

```kotlin
private const val BASE_URL = "https://api.brickognize.com/"
```

### Database

Room database is configured in:
`app/src/main/java/com/frootsnoops/brickognize/data/local/BrickDatabase.kt`

Current version: 1
Database name: `brick_database`

**Note**: Currently using `.fallbackToDestructiveMigration()` for development. Remove this for production to avoid data loss on schema changes.

## Key Classes and Responsibilities

### Data Layer

- **BrickDatabase**: Room database with all DAOs
- **BinLocationDao**: CRUD operations for bin locations
- **PartDao**: CRUD operations for LEGO parts
- **ScanDao**: Operations for scan history with candidates
- **BrickognizeApi**: Retrofit interface for API calls
- **Repositories**: Bridge between data sources and domain layer

### Domain Layer

- **Use Cases**:
  - `RecognizeImageUseCase`: Handles image recognition flow
  - `AssignBinToPartUseCase`: Assigns bins to parts
  - `GetAllBinLocationsUseCase`: Retrieves all bins
  - `GetScanHistoryUseCase`: Retrieves scan history
  - `GetPartsByBinUseCase`: Gets parts for a specific bin

### UI Layer

- **ViewModels**: State management for each screen
- **Screens**: Jetpack Compose UI for Home, Scan, Results, History, Bins
- **Navigation**: NavHost-based navigation between screens

## Usage

1. **Home Screen**: Select recognition mode (Parts/Sets/Figs)
2. **Scan**: Tap "Scan Brick" and select/capture an image
3. **Results**: View recognition results with confidence scores
4. **Assign Bins**: Tap "Assign Bin" on any result to create or select a bin location
5. **History**: Access past scans from the home screen
6. **Bins**: View all bins and parts assigned to each

## TODO / Future Improvements

- [ ] Implement full CameraX integration for live camera preview
- [ ] Add camera permissions handling with proper UI
- [ ] Implement proper migration strategy for Room database
- [ ] Add export/import functionality for local data
- [ ] Implement feedback submission to Brickognize API
- [ ] Add search/filter functionality for parts and bins
- [ ] Implement data sync preferences (e.g., auto-delete old scans)
- [ ] Add unit tests and instrumentation tests
- [ ] Create proper app icon and splash screen
- [ ] Add error reporting/logging
- [ ] Implement proper image caching strategy
- [ ] Add support for scanning multiple items in batch
- [ ] Create detailed scan report with statistics

## API Reference

This app uses the [Brickognize API](https://api.brickognize.com/) for LEGO recognition.

Endpoints used:
- `POST /predict/parts/` - Recognize LEGO parts
- `POST /predict/sets/` - Recognize LEGO sets  
- `POST /predict/figs/` - Recognize LEGO minifigures
- `POST /feedback/` - Submit recognition feedback (not yet implemented)

## License

This is a personal project for private use only. Not for distribution or commercial use.

## Notes

- This app is designed for **personal use** and will be **sideloaded**, not published on Play Store
- All data is stored **locally** on the device
- The app requires **internet connection** only for API recognition calls
- Camera integration is currently a **skeleton implementation** - image picker is used instead

---

**Built with ❤️ for LEGO enthusiasts**
