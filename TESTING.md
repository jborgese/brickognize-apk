# Testing Guide for Brickognize

## Testing Infrastructure

This project uses modern Android testing practices with JUnit 5 and supporting libraries.

### Testing Stack

**Unit Tests** (`src/test/`)
- **JUnit 5 (Jupiter)** - Modern test framework with better features
- **MockK** - Kotlin-friendly mocking library
- **Truth** - Fluent assertions library from Google
- **Turbine** - Testing library for Kotlin Flow
- **Coroutines Test** - Testing utilities for coroutines
- **Robolectric** - Fast Android unit tests without emulator

**Integration Tests** (`src/androidTest/`)
- **Room In-Memory Database** - Test database operations
- **Hilt Testing** - Dependency injection testing
- **Compose UI Testing** - Test Compose UI components

## Running Tests

### From Android Studio
1. Right-click on a test file or test method
2. Select "Run" or press `Ctrl+Shift+F10` (Windows/Linux) or `Cmd+Shift+R` (Mac)

### From Command Line

**Run all unit tests:**
```bash
./gradlew test
```

**Run specific test class:**
```bash
./gradlew test --tests com.frootsnoops.brickognize.ui.scan.ScanViewModelTest
```

**Run all instrumented tests (requires emulator/device):**
```bash
./gradlew connectedAndroidTest
```

**Run with coverage report:**
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

## Test Structure

### Unit Tests

**ViewModel Tests** - Test UI state management and business logic
- Located in `src/test/java/.../ui/`
- Mock dependencies with MockK
- Test state flows with Turbine
- Example: `ScanViewModelTest.kt`

**Use Case Tests** - Test domain layer business logic
- Located in `src/test/java/.../domain/usecase/`
- Mock repositories
- Verify business rules
- Example: `AssignBinToPartUseCaseTest.kt`

### Integration Tests

**Repository Tests** - Test data layer with real Room database
- Located in `src/androidTest/java/.../data/repository/`
- Use in-memory database
- Test CRUD operations and relationships
- Example: `PartRepositoryIntegrationTest.kt`

**UI Tests** - Test Compose screens
- Located in `src/androidTest/java/.../ui/`
- Test user interactions
- Verify UI elements
- Example: `HomeScreenTest.kt`

## Writing New Tests

### Unit Test Template (JUnit 5)

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("YourClass Tests")
class YourClassTest {

    private lateinit var subject: YourClass
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        // Initialize test dependencies
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        clearAllMocks()
    }

    @Test
    @DisplayName("Should do something when condition")
    fun `test name with spaces`() = runTest {
        // Given (Arrange)
        val input = "test"
        
        // When (Act)
        val result = subject.doSomething(input)
        
        // Then (Assert)
        assertThat(result).isEqualTo(expected)
    }
}
```

### Compose UI Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class YourScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screenDisplaysContent() {
        composeTestRule.setContent {
            YourScreen()
        }

        composeTestRule.onNodeWithText("Expected Text").assertExists()
        composeTestRule.onNodeWithTag("button").performClick()
    }
}
```

## Best Practices

1. **Use descriptive test names** - Use backticks for readable test names
2. **Follow AAA pattern** - Arrange, Act, Assert
3. **One assertion per test** - Or related assertions
4. **Test behavior, not implementation** - Focus on what, not how
5. **Mock external dependencies** - Keep tests fast and isolated
6. **Use Truth assertions** - More readable than JUnit assertions
7. **Test edge cases** - Not just happy paths
8. **Keep tests fast** - Use mocks, avoid real network/disk I/O in unit tests

## Example Test Commands

```bash
# Run all tests with detailed output
./gradlew test --info

# Run only ViewModel tests
./gradlew test --tests "*.ui.*ViewModelTest"

# Run only use case tests
./gradlew test --tests "*.usecase.*Test"

# Run tests and generate HTML report
./gradlew test
# Report: app/build/reports/tests/testDebugUnitTest/index.html
```

## Continuous Integration

For CI/CD pipelines, add to your workflow:

```yaml
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Instrumented Tests
  run: ./gradlew connectedAndroidTest
```

## Coverage

To enable code coverage, add to `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
    }
}
```

Then run:
```bash
./gradlew createDebugCoverageReport
```

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [MockK Documentation](https://mockk.io/)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Testing Coroutines](https://kotlinlang.org/docs/coroutines-guide.html#testing)
- [Truth Assertions](https://truth.dev/)
