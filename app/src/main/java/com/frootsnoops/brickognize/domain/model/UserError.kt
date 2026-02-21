package com.frootsnoops.brickognize.domain.model

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * User-facing error types with clear, actionable messages.
 * 
 * Converts technical exceptions into messages that users can understand and act upon.
 */
sealed class UserError {
    abstract val title: String
    abstract val message: String
    abstract val actionText: String?
    abstract val icon: ErrorIcon
    
    data class Network(
        override val title: String = "Connection Problem",
        override val message: String,
        override val actionText: String? = "Retry",
        override val icon: ErrorIcon = ErrorIcon.NO_WIFI
    ) : UserError()
    
    data class Server(
        override val title: String = "Server Issue",
        override val message: String,
        override val actionText: String? = "Try Again",
        override val icon: ErrorIcon = ErrorIcon.ERROR
    ) : UserError()
    
    data class NoResults(
        override val title: String = "Nothing Found",
        override val message: String,
        override val actionText: String? = "Scan Again",
        override val icon: ErrorIcon = ErrorIcon.SEARCH
    ) : UserError()
    
    data class InvalidImage(
        override val title: String = "Image Problem",
        override val message: String,
        override val actionText: String? = "Choose Another",
        override val icon: ErrorIcon = ErrorIcon.IMAGE
    ) : UserError()
    
    data class Storage(
        override val title: String = "Storage Error",
        override val message: String,
        override val actionText: String? = "OK",
        override val icon: ErrorIcon = ErrorIcon.STORAGE
    ) : UserError()
    
    data class Unknown(
        override val title: String = "Something Went Wrong",
        override val message: String,
        override val actionText: String? = "Dismiss",
        override val icon: ErrorIcon = ErrorIcon.ERROR
    ) : UserError()
}

/**
 * Icons to use with error messages (Material Icons).
 */
enum class ErrorIcon {
    NO_WIFI,      // Icons.Default.WifiOff
    ERROR,        // Icons.Default.Error
    SEARCH,       // Icons.Default.SearchOff
    IMAGE,        // Icons.Default.BrokenImage
    STORAGE,      // Icons.Default.SdCardAlert
}

/**
 * Extension function to convert technical exceptions to user-friendly errors.
 */
fun Throwable.toUserError(): UserError = when (this) {
    // Network connectivity issues
    is UnknownHostException -> UserError.Network(
        message = "No internet connection. Please check your WiFi or mobile data and try again."
    )
    
    is SocketTimeoutException -> UserError.Network(
        message = "The request timed out. The server might be slow or your connection is weak."
    )
    
    // File/storage issues (must be before IOException since FileNotFoundException extends it)
    is java.io.FileNotFoundException -> UserError.Storage(
        message = "The image file couldn't be found. Please select the image again."
    )
    
    is SecurityException -> UserError.Storage(
        message = "Permission denied. Please allow Brickognize to access your photos."
    )
    
    // Generic IO errors (after specific file exceptions)
    is IOException -> UserError.Network(
        message = "Network error occurred. Please check your connection and try again."
    )
    
    // HTTP errors (Retrofit)
    is HttpException -> when (code()) {
        400 -> UserError.InvalidImage(
            message = "The image couldn't be processed. Try taking a clearer photo with better lighting."
        )
        
        404 -> UserError.NoResults(
            message = "No LEGO items found in this image. Make sure the item is clearly visible and well-lit."
        )
        
        429 -> UserError.Server(
            message = "Too many requests. Please wait a minute before trying again.",
            actionText = "Wait"
        )
        
        500, 502, 503, 504 -> UserError.Server(
            message = "The Brickognize server is having issues. Please try again in a few moments."
        )
        
        else -> UserError.Server(
            message = "Server returned error code ${code()}. Please try again later."
        )
    }
    
    // Fallback for unknown errors
    else -> UserError.Unknown(
        message = this.message ?: "An unexpected error occurred. Please try again."
    )
}

/**
 * Extension to convert Result.Error to UserError
 */
fun Result.Error.toUserError(): UserError = this.exception.toUserError()
