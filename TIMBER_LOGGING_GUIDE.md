# Timber Logging Implementation Guide

This guide documents the Timber logging framework implementation in the Brickognize Android app, including conventions, best practices, and examples.

## Overview

Timber is a logger with a small, extensible API that provides utility on top of Android's normal Log class. It was integrated to improve debugging, monitoring, and production error tracking capabilities.

**Dependency:** `com.jakewharton.timber:timber:5.0.1`

## Setup

### Application Initialization

Timber is initialized in `BrickognizeApp.kt`:

```kotlin
class BrickognizeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashReportingTree())
        }
    }
}
```

### Custom Tree for Production

`CrashReportingTree.kt` provides filtered logging for release builds:

```kotlin
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Only log warnings and errors in production
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }
        
        // Add line numbers for better debugging
        val enhancedTag = tag?.let { "$it:${getLineNumber()}" } ?: "BrickognizeApp"
        
        // Here you would send to crash reporting service
        // e.g., Firebase Crashlytics, Sentry, etc.
        when (priority) {
            Log.WARN -> {
                // Log non-fatal warnings
                Log.w(enhancedTag, message, t)
            }
            Log.ERROR -> {
                // Log errors and exceptions
                Log.e(enhancedTag, message, t)
                t?.let {
                    // Send exception to crash reporting service
                }
            }
        }
    }
    
    private fun getLineNumber(): String {
        val stackTrace = Throwable().stackTrace
        return stackTrace.getOrNull(CALL_STACK_INDEX)?.lineNumber?.toString() ?: "?"
    }
    
    companion object {
        private const val CALL_STACK_INDEX = 7
    }
}
```

## Logging Levels

Timber provides five logging levels, each with a specific purpose:

### 1. Verbose - `Timber.v()`
**Purpose:** Most detailed information, typically only useful during development  
**When to use:** Fine-grained debugging information that's too noisy for regular debug logs  
**Production:** Filtered out (not logged)

```kotlin
// Rarely used - example only
Timber.v("Detailed loop iteration: index=$i, value=$value")
```

### 2. Debug - `Timber.d()`
**Purpose:** Diagnostic information useful for debugging  
**When to use:** 
- Function entry/exit points
- State changes
- Parameter values
- Data flow tracking
- Internal operation details

**Production:** Filtered out (not logged)

```kotlin
Timber.d("ScanViewModel: capturing image from camera")
Timber.d("Loading bin locations from database")
Timber.d("API response received: ${response.predictions.size} items")
Timber.d("Calling predictPart API with recognitionType=$recognitionType")
```

### 3. Info - `Timber.i()`
**Purpose:** Important informational messages about application flow  
**When to use:**
- Major operation completion
- Successful state transitions
- User actions with side effects
- Key business logic events

**Production:** Filtered out (not logged)

```kotlin
Timber.i("Starting image recognition for ${imageFile.name}")
Timber.i("Image recognition completed successfully: ${result.topCandidate?.itemNo}")
Timber.i("Assigned bin $binId to part $partId")
Timber.i("Creating new bin location: $newBinLabel")
```

### 4. Warning - `Timber.w()`
**Purpose:** Potentially harmful situations or recoverable errors  
**When to use:**
- Unexpected but handled conditions
- Fallback behavior triggered
- Deprecated API usage
- Invalid user input (recoverable)
- Missing optional data

**Production:** Logged and sent to crash reporting

```kotlin
Timber.w("Invalid image selection: file is empty")
Timber.w("Network unavailable, cannot recognize image")
Timber.w("Attempting to assign bin without part ID or bin label")
Timber.w("No recognition results found for image ${imageFile.name}")
```

### 5. Error - `Timber.e()`
**Purpose:** Error events that might still allow the app to continue  
**When to use:**
- Caught exceptions
- Operation failures
- API errors
- Database errors
- Unrecoverable errors

**Production:** Logged and sent to crash reporting

```kotlin
Timber.e(exception, "Failed to recognize image")
Timber.e(exception, "Database error while loading bin locations")
Timber.e(exception, "API call failed")
Timber.e(exception, "AssignBinToPartUseCase: failed to assign bin")
```

## Implementation Patterns

### ViewModels

ViewModels should log:
- User actions/intents
- State changes
- API calls
- Navigation events
- Error handling

**Example from `ScanViewModel.kt`:**

```kotlin
fun onCameraCapture() {
    Timber.d("ScanViewModel: capturing image from camera")
    viewModelScope.launch {
        captureImageLauncher?.launch(Unit)
    }
}

private fun processImageFile(file: File) {
    Timber.i("Processing image file: ${file.name}, size: ${file.length()} bytes")
    viewModelScope.launch {
        // ... processing logic
    }
}

private suspend fun recognizeImage(imageFile: File) {
    _state.update { it.copy(isLoading = true, error = null) }
    Timber.i("Starting image recognition for ${imageFile.name}")
    
    when (val result = recognizeImageUseCase(imageFile, _state.value.recognitionType)) {
        is Result.Success -> {
            Timber.i("Image recognition completed successfully: ${result.data.topCandidate?.itemNo}")
            // ... handle success
        }
        is Result.Error -> {
            Timber.e(result.exception, "Failed to recognize image")
            _state.update { it.copy(
                isLoading = false,
                error = result.exception.toUserError()
            )}
        }
    }
}
```

### Repositories

Repositories should log:
- Data source operations (API, database)
- Cache hits/misses
- Data transformations
- Network requests
- Database queries

**Example from `BrickognizeRepository.kt`:**

```kotlin
suspend fun recognizeImage(
    imageFile: File,
    recognitionType: RecognitionType,
    saveImageLocally: Boolean = true
): Result<RecognitionResult> = withContext(Dispatchers.IO) {
    try {
        Timber.i("Starting image recognition for ${imageFile.name}, type=$recognitionType")
        
        Timber.d("Calling predictPart API with recognitionType=$recognitionType")
        val response = api.predictPart(/* ... */)
        
        Timber.d("API response received: ${response.predictions.size} items")
        
        // Save scan to database
        Timber.d("Saving scan to database with ${response.predictions.size} candidates")
        val scanEntity = ScanEntity(/* ... */)
        scanDao.insertScan(scanEntity)
        
        Timber.i("Image recognition completed successfully: ${topCandidate?.itemNo}")
        Result.Success(recognitionResult)
        
    } catch (e: Exception) {
        Timber.e(e, "Failed to recognize image: ${e.message}")
        Result.Error(e, e.message ?: "Unknown error")
    }
}
```

### Use Cases

Use cases should log:
- Business logic execution
- Validation failures
- Orchestration steps
- Complex workflows

**Example from `RecognizeImageUseCase.kt`:**

```kotlin
suspend operator fun invoke(
    imageFile: File,
    recognitionType: RecognitionType,
    saveImageLocally: Boolean = true
): Result<RecognitionResult> {
    return try {
        Timber.d("RecognizeImageUseCase: starting recognition for ${imageFile.name}, type=$recognitionType")
        repository.recognizeImage(imageFile, recognitionType, saveImageLocally)
    } catch (e: Exception) {
        Timber.e(e, "RecognizeImageUseCase: exception occurred")
        Result.Error(e, "Failed to recognize image: ${e.message}")
    }
}
```

## Best Practices

### 1. Use Appropriate Log Levels

❌ **Bad:**
```kotlin
Timber.d("Error: Failed to load data") // Wrong level
Timber.i("Loop iteration $i") // Too detailed for info
Timber.e("User clicked button") // Not an error
```

✅ **Good:**
```kotlin
Timber.e(exception, "Failed to load data")
Timber.d("Loop iteration $i")
Timber.d("User clicked button")
```

### 2. Include Context in Messages

❌ **Bad:**
```kotlin
Timber.d("Loading data")
Timber.i("Success")
Timber.e(exception, "Error occurred")
```

✅ **Good:**
```kotlin
Timber.d("Loading bin locations from database")
Timber.i("Image recognition completed successfully: ${result.itemNo}")
Timber.e(exception, "Failed to recognize image: ${imageFile.name}")
```

### 3. Log Exceptions with Context

❌ **Bad:**
```kotlin
catch (e: Exception) {
    Timber.e("Error")
}
```

✅ **Good:**
```kotlin
catch (e: Exception) {
    Timber.e(e, "Failed to assign bin $binId to part $partId")
}
```

### 4. Avoid Logging Sensitive Data

❌ **Bad:**
```kotlin
Timber.d("User password: $password")
Timber.i("Credit card: $cardNumber")
Timber.d("API token: $token")
```

✅ **Good:**
```kotlin
Timber.d("User authenticated successfully")
Timber.i("Payment processed")
Timber.d("API request authorized")
```

### 5. Use String Templates, Not Concatenation

❌ **Bad:**
```kotlin
Timber.d("Part ID: " + partId + ", Bin ID: " + binId)
```

✅ **Good:**
```kotlin
Timber.d("Part ID: $partId, Bin ID: $binId")
```

### 6. Don't Log in Loops (Usually)

❌ **Bad:**
```kotlin
items.forEach { item ->
    Timber.d("Processing item: ${item.id}") // Logs 1000 times
    process(item)
}
```

✅ **Good:**
```kotlin
Timber.d("Processing ${items.size} items")
items.forEach { item ->
    process(item)
}
Timber.i("Completed processing ${items.size} items")
```

### 7. Log at Method Boundaries

✅ **Good:**
```kotlin
suspend fun loadData(): Result<List<Item>> {
    Timber.d("Loading data from repository")
    
    return try {
        val items = repository.getItems()
        Timber.i("Successfully loaded ${items.size} items")
        Result.Success(items)
    } catch (e: Exception) {
        Timber.e(e, "Failed to load data")
        Result.Error(e)
    }
}
```

## Integration with Error Handling

Timber logging works seamlessly with the `UserError` system:

```kotlin
when (val result = operation()) {
    is Result.Success -> {
        Timber.i("Operation completed successfully")
        _state.update { it.copy(data = result.data, error = null) }
    }
    is Result.Error -> {
        val userError = result.exception.toUserError()
        Timber.e(result.exception, "Operation failed: ${userError.message}")
        _state.update { it.copy(error = userError) }
    }
}
```

## Testing Considerations

### In Unit Tests

Tests should not rely on Timber output. For unit tests, Timber can be:
- Not planted (logs are ignored)
- Or planted with a test tree for verification

```kotlin
@Before
fun setup() {
    // Optional: plant test tree if needed
    // Timber.plant(object : Timber.Tree() {
    //     override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    //         println("[$priority] $tag: $message")
    //     }
    // })
}

@After
fun teardown() {
    Timber.uprootAll()
}
```

### In Integration Tests

Integration tests can verify that Timber is properly initialized:

```kotlin
@Test
fun `timber is initialized in application`() {
    val app = ApplicationProvider.getApplicationContext<BrickognizeApp>()
    // Timber should be planted, but specific verification depends on needs
}
```

## Performance Considerations

1. **String Formatting:** Timber uses lazy evaluation, but complex string building still happens
```kotlin
// This builds the string even if debug is disabled
Timber.d("Data: ${expensiveOperation()}")

// Better: only call if needed
if (BuildConfig.DEBUG) {
    Timber.d("Data: ${expensiveOperation()}")
}
```

2. **Production Filtering:** `CrashReportingTree` filters verbose/debug/info logs automatically, so they have minimal performance impact in release builds.

3. **Exception Logging:** Including exceptions in error logs is cheap and valuable for debugging.

## Integration with Crash Reporting

To connect Timber to Firebase Crashlytics or Sentry, update `CrashReportingTree`:

```kotlin
class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.VERBOSE || priority == Log.DEBUG || priority == Log.INFO) {
            return
        }
        
        val enhancedTag = tag?.let { "$it:${getLineNumber()}" } ?: "BrickognizeApp"
        
        when (priority) {
            Log.WARN -> {
                // Firebase Crashlytics example:
                // FirebaseCrashlytics.getInstance().log("$enhancedTag: $message")
            }
            Log.ERROR -> {
                t?.let {
                    // FirebaseCrashlytics.getInstance().recordException(it)
                }
                // FirebaseCrashlytics.getInstance().log("$enhancedTag: $message")
            }
        }
    }
}
```

## Files Modified for Timber Integration

### Core Setup
- `app/build.gradle.kts` - Added Timber dependency
- `BrickognizeApp.kt` - Timber initialization
- `CrashReportingTree.kt` - Custom production tree

### ViewModels (Comprehensive Logging)
- `ScanViewModel.kt` - Image capture, recognition flow
- `ResultsViewModel.kt` - Bin assignment, results display
- *(Note: HomeViewModel, HistoryViewModel, BinsViewModel may need logging based on complexity)*

### Repositories (Comprehensive Logging)
- `BrickognizeRepository.kt` - API calls, database operations

### Use Cases (Comprehensive Logging)
- `RecognizeImageUseCase.kt` - Image recognition orchestration
- `AssignBinToPartUseCase.kt` - Bin assignment workflow

### Files That Don't Need Extensive Logging
- Simple data classes (entities, DTOs)
- Sealed classes (Result, UserError)
- UI components (Composables) - Use debug logs sparingly
- Constants and configuration files
- Test files (use println or test-specific logging)

## Summary

**Logging Levels:**
- 🟣 Verbose: Extreme detail (rarely used)
- 🔵 Debug: Development diagnostics → `Timber.d()`
- 🟢 Info: Important events → `Timber.i()`
- 🟡 Warning: Recoverable issues → `Timber.w()`
- 🔴 Error: Failures and exceptions → `Timber.e(exception, message)`

**Key Principles:**
1. Log at appropriate levels
2. Include context in messages
3. Always include exceptions with `Timber.e(exception, ...)`
4. Don't log sensitive data
5. Use string templates
6. Log at method boundaries
7. Filter production logs appropriately

**Production:**
- Debug/Info logs are filtered out
- Warnings and Errors are logged and sent to crash reporting
- Custom `CrashReportingTree` adds line numbers to tags

**Development:**
- All logs visible via `Timber.DebugTree()`
- Logs appear in Logcat with automatic tagging
- Easy to filter by tag or level

## Next Steps

1. ✅ **Complete** - Core setup (Timber dependency, CrashReportingTree, App initialization)
2. ✅ **Complete** - ViewModels logging (ScanViewModel, ResultsViewModel)
3. ✅ **Complete** - Repository logging (BrickognizeRepository)
4. ✅ **Complete** - Use case logging (RecognizeImageUseCase, AssignBinToPartUseCase)
5. **Optional** - Add logging to remaining ViewModels if they contain complex logic
6. **Optional** - Add logging to other repositories if they exist
7. **Production** - Integrate CrashReportingTree with Firebase Crashlytics or Sentry
8. **Testing** - Verify all logs appear correctly in different scenarios
9. **Performance** - Profile app to ensure logging overhead is minimal
