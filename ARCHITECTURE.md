# Brickognize App - Architecture Diagram

## High-Level Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                       USER DEVICE                             │
│                                                                │
│  ┌────────────────────────────────────────────────────────┐  │
│  │                   PRESENTATION                          │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌────────┐ │  │
│  │  │  Home    │  │   Scan   │  │ Results  │  │History │ │  │
│  │  │  Screen  │  │  Screen  │  │  Screen  │  │ Screen │ │  │
│  │  └────┬─────┘  └────┬─────┘  └────┬─────┘  └───┬────┘ │  │
│  │       │             │             │            │        │  │
│  │  ┌────▼─────────────▼─────────────▼────────────▼────┐  │  │
│  │  │              ViewModels (Hilt)                    │  │  │
│  │  └────┬──────────────────────────────────────────────┘  │  │
│  └───────┼──────────────────────────────────────────────────┘  │
│          │                                                      │
│  ┌───────▼──────────────────────────────────────────────────┐  │
│  │                      DOMAIN                              │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │              Use Cases                              │ │  │
│  │  │  • RecognizeImageUseCase                           │ │  │
│  │  │  • AssignBinToPartUseCase                          │ │  │
│  │  │  • GetAllBinLocationsUseCase                       │ │  │
│  │  │  • GetScanHistoryUseCase                           │ │  │
│  │  │  • GetPartsByBinUseCase                            │ │  │
│  │  └────┬───────────────────────────────────────────────┘ │  │
│  │       │                                                  │  │
│  │  ┌────▼───────────────────────────────────────────────┐ │  │
│  │  │           Domain Models (Pure Kotlin)              │ │  │
│  │  │  BrickItem | BinLocation | RecognitionResult      │ │  │
│  │  └────────────────────────────────────────────────────┘ │  │
│  └───────┬──────────────────────────────────────────────────┘  │
│          │                                                      │
│  ┌───────▼──────────────────────────────────────────────────┐  │
│  │                       DATA                               │  │
│  │  ┌────────────────────────────────────────────────────┐ │  │
│  │  │              Repositories                           │ │  │
│  │  │  • BrickognizeRepository                           │ │  │
│  │  │  • BinLocationRepository                           │ │  │
│  │  │  • PartRepository                                  │ │  │
│  │  │  • ScanRepository                                  │ │  │
│  │  └─────┬────────────────────────────────┬─────────────┘ │  │
│  │        │                                │                │  │
│  │  ┌─────▼──────────┐            ┌───────▼──────────────┐ │  │
│  │  │  Room Database │            │   Retrofit + OkHttp  │ │  │
│  │  │                │            │                      │ │  │
│  │  │ • Parts        │            │  BrickognizeApi      │ │  │
│  │  │ • Bins         │            │                      │ │  │
│  │  │ • Scans        │            │  • predictPart()     │ │  │
│  │  │ • Candidates   │            │  • predictSet()      │ │  │
│  │  └────────────────┘            │  • predictFig()      │ │  │
│  │                                │  • sendFeedback()    │ │  │
│  │                                └──────────┬───────────┘ │  │
│  └──────────────────────────────────────────┼──────────────┘  │
└─────────────────────────────────────────────┼─────────────────┘
                                              │
                                              │ HTTPS
                                              │
                                   ┌──────────▼──────────┐
                                   │  Brickognize API    │
                                   │                     │
                                   │  api.brickognize    │
                                   │       .com          │
                                   └─────────────────────┘
```

## Data Flow: Image Recognition

```
1. User Action
   ↓
┌──────────────────┐
│  ScanScreen      │  User selects image
│  (Compose UI)    │
└────────┬─────────┘
         │ ImageUri
         ↓
┌────────────────────┐
│  ScanViewModel     │  processImage(uri)
└────────┬───────────┘
         │
         ↓
┌──────────────────────────┐
│ RecognizeImageUseCase    │  invoke(imageFile, type)
└────────┬─────────────────┘
         │
         ↓
┌────────────────────────────┐
│ BrickognizeRepository      │  recognizeImage()
│                            │
│  1. Call API               │ ──────→ Retrofit → Brickognize API
│  2. Parse response         │ ←────── JSON Response (DTO)
│  3. Save to Room           │ ──────→ Room DB (Entities)
│  4. Load bin info          │ ←────── Room DB (with Relations)
│  5. Return domain model    │
└────────┬───────────────────┘
         │ Result<RecognitionResult>
         ↓
┌────────────────────────────┐
│  ScanViewModel             │  Updates uiState
└────────┬───────────────────┘
         │
         ↓
┌────────────────────────────┐
│  Navigation                │  Navigate to Results
└────────┬───────────────────┘
         │
         ↓
┌────────────────────────────┐
│  ResultsScreen             │  Display with bin info
│  (Compose UI)              │
└────────────────────────────┘
```

## Database Schema (Room)

```
┌─────────────────────┐
│  bin_locations      │
├─────────────────────┤
│ • id (PK)           │
│ • label             │
│ • description       │
│ • created_at        │
└─────────┬───────────┘
          │
          │ 1:N
          │
┌─────────▼───────────┐
│  parts              │
├─────────────────────┤
│ • id (PK)           │
│ • name              │
│ • type              │
│ • category          │
│ • img_url           │
│ • bin_location_id   │ (FK → bin_locations.id)
│ • created_at        │
│ • updated_at        │
└─────────┬───────────┘
          │
          │ 1:N
          │
┌─────────▼───────────────┐
│  scan_candidates        │
├─────────────────────────┤
│ • scan_id (PK, FK)      │──┐
│ • item_id (PK, FK)      │  │
│ • rank                  │  │
│ • score                 │  │
└─────────────────────────┘  │
                             │
                             │ N:1
                             │
                    ┌────────▼─────────┐
                    │  scans           │
                    ├──────────────────┤
                    │ • id (PK)        │
                    │ • timestamp      │
                    │ • image_path     │
                    │ • listing_id     │
                    │ • top_item_id    │
                    │ • notes          │
                    │ • recognition_   │
                    │   type           │
                    └──────────────────┘
```

## Dependency Injection Graph (Hilt)

```
┌──────────────────────────────┐
│  @HiltAndroidApp             │
│  BrickognizeApp              │
└──────────────┬───────────────┘
               │
               │ provides
               ↓
┌──────────────────────────────────────────┐
│         SingletonComponent               │
├──────────────────────────────────────────┤
│                                          │
│  DatabaseModule                          │
│  ├─→ BrickDatabase                       │
│  ├─→ BinLocationDao                      │
│  ├─→ PartDao                             │
│  └─→ ScanDao                             │
│                                          │
│  NetworkModule                           │
│  ├─→ OkHttpClient                        │
│  ├─→ Gson                                │
│  ├─→ Retrofit                            │
│  └─→ BrickognizeApi                      │
│                                          │
│  RepositoryModule                        │
│  ├─→ BinLocationRepository               │
│  ├─→ PartRepository                      │
│  ├─→ ScanRepository                      │
│  └─→ BrickognizeRepository               │
│                                          │
│  Use Cases (auto-injected)               │
│  ├─→ RecognizeImageUseCase               │
│  ├─→ AssignBinToPartUseCase              │
│  ├─→ GetAllBinLocationsUseCase           │
│  ├─→ GetScanHistoryUseCase               │
│  └─→ GetPartsByBinUseCase                │
│                                          │
└──────────────┬───────────────────────────┘
               │
               │ injects into
               ↓
┌──────────────────────────────┐
│  @AndroidEntryPoint          │
│  MainActivity                │
└──────────────┬───────────────┘
               │
               │ creates
               ↓
┌──────────────────────────────┐
│  ViewModels (@HiltViewModel) │
│  ├─→ HomeViewModel           │
│  ├─→ ScanViewModel           │
│  ├─→ ResultsViewModel        │
│  ├─→ HistoryViewModel        │
│  └─→ BinsViewModel           │
└──────────────────────────────┘
```

## Navigation Graph

```
┌─────────────┐
│   Home      │ ◄─── Start Destination
└──────┬──────┘
       │
       ├─────────────────────────────┐
       │                             │
       ↓                             ↓
┌──────────────┐              ┌──────────────┐
│   Scan       │              │   History    │
└──────┬───────┘              └──────────────┘
       │
       │ (on success)
       ↓
┌──────────────┐
│  Results     │
│              │
│  ┌────────┐  │
│  │  Bin   │  │ ← Dialog/BottomSheet
│  │ Picker │  │   (not a separate screen)
│  └────────┘  │
└──────────────┘

┌──────────────┐
│    Bins      │
│              │
│  ┌────────┐  │
│  │  Bin   │  │ ← Nested view
│  │Details │  │   (same screen)
│  └────────┘  │
└──────────────┘
```

## Key Design Patterns

### 1. Repository Pattern
```
ViewModel → Repository → [Data Source A, Data Source B]
                       ↓
                  Single Source of Truth
```

### 2. Use Case Pattern
```
ViewModel → Use Case → [Repository A, Repository B]
                    ↓
            Orchestrated Business Logic
```

### 3. MVVM Pattern
```
View (Compose) ←──observes──→ ViewModel ←──uses──→ Repository
      ↓                           ↓                     ↓
   UI State                  Business Logic         Data Access
```

### 4. Unidirectional Data Flow
```
User Action → ViewModel → Repository → Data Source
                ↓                           ↓
            UI State ← Domain Model ← Raw Data
                ↓
            Recompose UI
```

## Technology Stack Summary

| Layer | Technologies |
|-------|-------------|
| **UI** | Jetpack Compose, Material 3, Coil |
| **Navigation** | Navigation Compose |
| **State** | ViewModel, StateFlow, LiveData |
| **DI** | Hilt (Dagger) |
| **Database** | Room SQLite |
| **Network** | Retrofit, OkHttp, Gson |
| **Async** | Kotlin Coroutines, Flow |
| **Build** | Gradle Kotlin DSL, KSP |
| **Language** | Kotlin 1.9.20 |

---

This diagram provides a visual representation of the complete app architecture.
For implementation details, see the source code and BUILD_GUIDE.md.
