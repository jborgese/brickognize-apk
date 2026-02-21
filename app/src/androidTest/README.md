# Android Instrumented Tests

This directory contains **instrumented tests** (UI tests and integration tests) that run on an Android device or emulator.

## Test Structure

```
androidTest/
├── data/
│   ├── local/
│   │   └── BrickDatabaseTest.kt              # Database schema and queries
│   └── repository/
│       ├── BinLocationRepositoryIntegrationTest.kt
│       ├── PartRepositoryIntegrationTest.kt
│       └── ScanRepositoryIntegrationTest.kt
├── ui/
│   ├── bins/
│   │   └── BinsScreenTest.kt                 # Bins UI tests
│   ├── history/
│   │   └── HistoryScreenTest.kt              # History UI tests
│   ├── home/
│   │   └── HomeScreenTest.kt                 # Home UI tests
│   ├── results/
│   │   └── ResultsScreenTest.kt              # Results UI tests
│   └── scan/
│       └── ScanScreenTest.kt                 # Scan UI tests
├── BrickognizeAppE2ETest.kt                  # End-to-end integration tests
└── HiltTestRunner.kt                         # Hilt test configuration
```

## Test Categories

### 1. UI Tests (Compose)
Tests for individual Compose screens using `createComposeRule()`:
- **HomeScreenTest**: Navigation, button clicks, text display
- **ScanScreenTest**: Image selection UI
- **ResultsScreenTest**: Recognition results display, loading/error states
- **HistoryScreenTest**: Scan history list, empty states
- **BinsScreenTest**: Bin management UI, parts list

### 2. Integration Tests (Repository + Database)
Tests that verify data layer integration with real Room database:
- **PartRepositoryIntegrationTest**: Part CRUD operations, bin assignment
- **BinLocationRepositoryIntegrationTest**: Bin CRUD operations
- **ScanRepositoryIntegrationTest**: Scan history with candidates
- **BrickDatabaseTest**: Database schema, foreign keys, transactions

### 3. End-to-End Tests
Full app flow tests using `createAndroidComposeRule()`:
- **BrickognizeAppE2ETest**: Complete navigation flows, back stack, state preservation

## Running Tests

### From Android Studio
1. Right-click on `androidTest` folder
2. Select **Run 'Tests in androidTest'**
3. Choose device/emulator
4. Wait for results

### From Command Line

#### Run all instrumented tests:
```bash
./gradlew connectedAndroidTest
```

#### Run specific test class:
```bash
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.frootsnoops.brickognize.ui.home.HomeScreenTest
```

#### Run tests with coverage:
```bash
./gradlew connectedAndroidTest -Pcoverage
```

### View Reports
After running tests, reports are generated at:
```
app/build/reports/androidTests/connected/index.html
```

## Requirements

### Device/Emulator
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 35 (Android 15)
- **Recommended**: Pixel 5 or newer emulator

### Test Dependencies (Already Configured)
```kotlin
// JUnit & Android Test
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

// Compose Testing
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
debugImplementation("androidx.compose.ui:ui-test-manifest")

// Hilt Testing
androidTestImplementation("com.google.dagger:hilt-android-testing:2.52")
kspAndroidTest("com.google.dagger:hilt-android-compiler:2.52")

// MockK
androidTestImplementation("io.mockk:mockk-android:1.13.8")

// Truth
androidTestImplementation("com.google.truth:truth:1.1.5")

// Coroutines Test
androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
```

## Writing New Tests

### UI Test Template
```kotlin
@RunWith(AndroidJUnit4::class)
class MyScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSomething() {
        composeTestRule.setContent {
            BrickognizeTheme {
                MyScreen(/* params */)
            }
        }
        
        composeTestRule.onNodeWithText("Text").assertExists()
    }
}
```

### Integration Test Template
```kotlin
@RunWith(AndroidJUnit4::class)
class MyRepositoryIntegrationTest {
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
    fun testSomething() = runTest {
        // Test code
    }
}
```

## Test Statistics

| Category | Tests | Coverage |
|----------|-------|----------|
| **UI Tests** | 40+ tests | All screens |
| **Integration Tests** | 30+ tests | All repositories |
| **Database Tests** | 10+ tests | Schema & queries |
| **E2E Tests** | 8 tests | Full app flows |
| **Total** | **88+ tests** | Comprehensive |

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Instrumented Tests
  uses: reactivecircus/android-emulator-runner@v2
  with:
    api-level: 34
    target: google_apis
    arch: x86_64
    script: ./gradlew connectedAndroidTest
```

## Debugging Failed Tests

### Enable Test Logs
```bash
./gradlew connectedAndroidTest --info
```

### Screenshot on Failure
Tests automatically capture screenshots on failure in:
```
app/build/outputs/androidTest-results/connected/
```

### Logcat Output
View test logs during execution:
```bash
adb logcat -c && adb logcat TestRunner:V *:S
```

## Best Practices

1. ✅ **Use `createComposeRule()`** for isolated UI tests
2. ✅ **Use `createAndroidComposeRule()`** for E2E tests with Hilt
3. ✅ **Use in-memory database** for fast integration tests
4. ✅ **Test user flows**, not implementation details
5. ✅ **Verify accessibility** with content descriptions
6. ✅ **Test error states** and edge cases
7. ✅ **Keep tests independent** - no shared state
8. ✅ **Use meaningful test names** describing behavior

## Performance Tips

- Tests run on emulator: ~2-5 minutes for full suite
- Use `allowMainThreadQueries()` only in tests
- In-memory database is much faster than persisted
- Parallel test execution with multiple devices

## Troubleshooting

### Tests won't run
```bash
# Clean and rebuild
./gradlew clean
./gradlew :app:assembleDebugAndroidTest
./gradlew connectedAndroidTest
```

### Hilt injection fails
- Verify `HiltTestRunner` is configured in `build.gradle.kts`
- Check `@HiltAndroidTest` annotation on test class
- Call `hiltRule.inject()` in `@Before` method

### Compose tests fail
- Ensure `debugImplementation("androidx.compose.ui:ui-test-manifest")` is added
- Use `composeTestRule.waitForIdle()` for async operations
- Check semantic nodes with `printToLog()`

## Further Reading

- [Compose Testing Guide](https://developer.android.com/jetpack/compose/testing)
- [Hilt Testing](https://developer.android.com/training/dependency-injection/hilt-testing)
- [Room Testing](https://developer.android.com/training/data-storage/room/testing-db)
- [Espresso Basics](https://developer.android.com/training/testing/espresso)
