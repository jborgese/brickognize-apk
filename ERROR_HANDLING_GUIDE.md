# Error Handling Guide

## Overview

This guide explains the comprehensive error handling system implemented in Brickognize.

---

## Architecture

### Error Flow

```
Technical Exception → UserError → UI Component → User Action
     (Network)          (Message)    (ErrorCard)    (Retry)
```

1. **Technical Exception** - Raw error from system/network (e.g., `SocketTimeoutException`)
2. **UserError** - User-friendly error with clear message and action
3. **UI Component** - Visual representation (`ErrorCard`, `InlineErrorMessage`, etc.)
4. **User Action** - Actionable button (Retry, Choose Another, etc.)

---

## Error Types

### 1. Network Errors

**When:** Internet connectivity issues, DNS problems, timeouts

**User Sees:**
- 🚫 **No WiFi Icon**
- **Title:** "Connection Problem"
- **Message:** "No internet connection. Please check your WiFi or mobile data and try again."
- **Action:** "Retry"

**Technical Causes:**
- `UnknownHostException`
- `SocketTimeoutException`
- `IOException`

### 2. Server Errors

**When:** Brickognize API issues, rate limiting, server downtime

**User Sees:**
- ⚠️ **Error Icon**
- **Title:** "Server Issue"
- **Message:** Context-specific (e.g., "Too many requests", "Server maintenance")
- **Action:** "Try Again" or "Wait"

**Technical Causes:**
- HTTP 429 (Rate Limit)
- HTTP 500, 502, 503, 504 (Server Errors)

### 3. No Results

**When:** API successfully processed image but found no matches

**User Sees:**
- 🔍 **Search Icon**
- **Title:** "Nothing Found"
- **Message:** "No LEGO items found in this image. Make sure the item is clearly visible and well-lit."
- **Action:** "Scan Again"

**Technical Causes:**
- HTTP 404 from Brickognize API
- Empty results array

### 4. Invalid Image

**When:** Image processing fails, corrupt file, unsupported format

**User Sees:**
- 🖼️ **Broken Image Icon**
- **Title:** "Image Problem"
- **Message:** "The image couldn't be processed. Try taking a clearer photo with better lighting."
- **Action:** "Choose Another"

**Technical Causes:**
- HTTP 400 (Bad Request)
- Invalid file format
- Corrupt image data

### 5. Storage Errors

**When:** File system issues, permission denied

**User Sees:**
- 💾 **Storage Alert Icon**
- **Title:** "Storage Error"
- **Message:** "Permission denied. Please allow Brickognize to access your photos."
- **Action:** "OK"

**Technical Causes:**
- `FileNotFoundException`
- `SecurityException`

### 6. Unknown Errors

**When:** Unexpected exceptions not covered above

**User Sees:**
- ⚠️ **Error Icon**
- **Title:** "Something Went Wrong"
- **Message:** Generic error message with exception details
- **Action:** "Dismiss"

**Technical Causes:**
- Any uncaught exception

---

## Usage in Code

### Converting Exceptions to User Errors

```kotlin
import com.frootsnoops.brickognize.domain.model.toUserError

// In ViewModel
catch (e: Exception) {
    val userError = e.toUserError()
    _uiState.value = ScanUiState.Error(userError)
}

// From Result.Error
when (result) {
    is Result.Error -> {
        val userError = result.toUserError()
        showError(userError)
    }
}
```

### Displaying Errors in UI

#### Option 1: Error Card (Prominent)

```kotlin
import com.frootsnoops.brickognize.ui.components.ErrorCard

@Composable
fun MyScreen(uiState: UiState) {
    when (uiState) {
        is UiState.Error -> {
            ErrorCard(
                error = uiState.error,
                onAction = { viewModel.retry() },
                onDismiss = { viewModel.clearError() }
            )
        }
    }
}
```

#### Option 2: Inline Error (Subtle)

```kotlin
import com.frootsnoops.brickognize.ui.components.InlineErrorMessage

@Composable
fun MyList(items: List<Item>, error: UserError?) {
    LazyColumn {
        if (error != null) {
            item {
                InlineErrorMessage(error = error)
            }
        }
        items(items) { item ->
            ItemCard(item)
        }
    }
}
```

#### Option 3: Snackbar (Temporary)

```kotlin
import com.frootsnoops.brickognize.ui.components.ErrorSnackbar

@Composable
fun MyScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(uiState) {
        if (uiState is UiState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = uiState.error.message,
                    actionLabel = uiState.error.actionText
                )
            }
        }
    }
    
    Scaffold(
        snackbarHost = {
            ErrorSnackbar(
                error = currentError,
                snackbarHostState = snackbarHostState,
                onAction = { viewModel.retry() }
            )
        }
    ) {
        // Content
    }
}
```

---

## ViewModel Pattern

### Define UI State with UserError

```kotlin
sealed class MyUiState {
    data object Loading : MyUiState()
    data class Success(val data: MyData) : MyUiState()
    data class Error(val error: UserError) : MyUiState()
}
```

### Handle Errors in ViewModel

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val useCase: MyUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<MyUiState>(MyUiState.Loading)
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = MyUiState.Loading
            
            when (val result = useCase()) {
                is Result.Success -> {
                    _uiState.value = MyUiState.Success(result.data)
                }
                is Result.Error -> {
                    val userError = result.toUserError()
                    _uiState.value = MyUiState.Error(userError)
                }
            }
        }
    }
    
    fun retry() {
        loadData()
    }
    
    fun clearError() {
        _uiState.value = MyUiState.Loading
    }
}
```

---

## Testing Error Handling

### Unit Tests

```kotlin
@Test
fun `socket timeout converts to network error`() {
    val exception = SocketTimeoutException("timeout")
    val userError = exception.toUserError()
    
    assertThat(userError).isInstanceOf(UserError.Network::class.java)
    assertThat(userError.message).contains("timed out")
    assertThat(userError.actionText).isEqualTo("Retry")
}

@Test
fun `http 404 converts to no results error`() {
    val exception = HttpException(
        Response.error<Any>(404, ResponseBody.create(null, ""))
    )
    val userError = exception.toUserError()
    
    assertThat(userError).isInstanceOf(UserError.NoResults::class.java)
    assertThat(userError.message).contains("No LEGO items found")
}
```

### UI Tests

```kotlin
@Test
fun errorCard_displaysCorrectMessage() {
    composeTestRule.setContent {
        ErrorCard(
            error = UserError.Network(
                message = "Test error message"
            ),
            onAction = { }
        )
    }
    
    composeTestRule.onNodeWithText("Connection Problem").assertExists()
    composeTestRule.onNodeWithText("Test error message").assertExists()
    composeTestRule.onNodeWithText("Retry").assertExists()
}
```

---

## Custom Error Messages

You can create custom error messages for specific scenarios:

```kotlin
// Custom network error
val customNetworkError = UserError.Network(
    title = "Upload Failed",
    message = "Your scan couldn't be uploaded. Check your connection and try again.",
    actionText = "Retry Upload"
)

// Custom no results
val noPartsError = UserError.NoResults(
    message = "No parts detected. Try photographing individual LEGO pieces against a plain background."
)
```

---

## Best Practices

### ✅ Do's

1. **Always convert exceptions to UserError** before showing to user
2. **Provide actionable buttons** (Retry, Choose Another, etc.)
3. **Use appropriate icons** for visual context
4. **Keep messages concise** (1-2 sentences)
5. **Suggest solutions** when possible
6. **Log technical details** for debugging (separate from user message)

### ❌ Don'ts

1. **Don't show raw exception messages** to users
2. **Don't use technical jargon** (SocketTimeoutException, HTTP 500)
3. **Don't leave users stuck** (always provide an action)
4. **Don't be vague** ("Error occurred" - what error?)
5. **Don't blame the user** ("You did something wrong")

---

## Error Message Writing Guidelines

### Formula

```
[What happened] + [Why it happened] + [What to do]
```

### Examples

**Good:**
> "The request timed out. The server might be slow or your connection is weak. Try again or check your WiFi."

**Bad:**
> "SocketTimeoutException: timeout"

**Good:**
> "No LEGO items found in this image. Make sure the item is clearly visible and well-lit. Try taking another photo."

**Bad:**
> "404 Not Found"

---

## Logging Errors

While showing user-friendly messages, still log technical details:

```kotlin
import timber.log.Timber

catch (e: Exception) {
    // Log technical details
    Timber.e(e, "Image recognition failed: ${e.message}")
    
    // Show user-friendly message
    val userError = e.toUserError()
    _uiState.value = UiState.Error(userError)
}
```

---

## Resources

- [Material Design Error States](https://m3.material.io/components/snackbar/overview)
- [Android Error Handling Best Practices](https://developer.android.com/topic/architecture/data-layer#handle-errors)
- [UX Writing for Error Messages](https://uxplanet.org/how-to-write-error-messages-that-dont-suck-f02d14e2c1f8)

---

**Last Updated:** December 3, 2025  
**Version:** 1.0  
**Maintainer:** Brickognize Development Team
