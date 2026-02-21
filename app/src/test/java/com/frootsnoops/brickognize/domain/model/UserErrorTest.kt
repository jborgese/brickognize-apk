package com.frootsnoops.brickognize.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.FileNotFoundException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Tests for error handling conversion from technical exceptions to user-friendly errors.
 */
@DisplayName("UserError Conversion Tests")
class UserErrorTest {
    
    @Test
    @DisplayName("UnknownHostException converts to Network error")
    fun `unknown host exception converts to network error`() {
        val exception = UnknownHostException("api.brickognize.com")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Network::class.java)
        assertThat(userError.title).isEqualTo("Connection Problem")
        assertThat(userError.message).contains("No internet connection")
        assertThat(userError.actionText).isEqualTo("Retry")
        assertThat(userError.icon).isEqualTo(ErrorIcon.NO_WIFI)
    }
    
    @Test
    @DisplayName("SocketTimeoutException converts to Network error")
    fun `socket timeout exception converts to network error`() {
        val exception = SocketTimeoutException("timeout")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Network::class.java)
        assertThat(userError.message).contains("timed out")
        assertThat(userError.message).contains("slow")
        assertThat(userError.actionText).isEqualTo("Retry")
    }
    
    @Test
    @DisplayName("Generic IOException converts to Network error")
    fun `generic io exception converts to network error`() {
        val exception = IOException("Connection reset")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Network::class.java)
        assertThat(userError.message).contains("Network error")
    }
    
    @Test
    @DisplayName("HTTP 400 converts to InvalidImage error")
    fun `http 400 converts to invalid image error`() {
        val exception = createHttpException(400)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.InvalidImage::class.java)
        assertThat(userError.message).contains("couldn't be processed")
        assertThat(userError.message).contains("clearer photo")
        assertThat(userError.actionText).isEqualTo("Choose Another")
        assertThat(userError.icon).isEqualTo(ErrorIcon.IMAGE)
    }
    
    @Test
    @DisplayName("HTTP 404 converts to NoResults error")
    fun `http 404 converts to no results error`() {
        val exception = createHttpException(404)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.NoResults::class.java)
        assertThat(userError.message).contains("No LEGO items found")
        assertThat(userError.message).contains("clearly visible")
        assertThat(userError.actionText).isEqualTo("Scan Again")
        assertThat(userError.icon).isEqualTo(ErrorIcon.SEARCH)
    }
    
    @Test
    @DisplayName("HTTP 429 converts to Server error with rate limit message")
    fun `http 429 converts to server error with rate limit message`() {
        val exception = createHttpException(429)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("Too many requests")
        assertThat(userError.message).contains("wait a minute")
        assertThat(userError.actionText).isEqualTo("Wait")
    }
    
    @Test
    @DisplayName("HTTP 500 converts to Server error")
    fun `http 500 converts to server error`() {
        val exception = createHttpException(500)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("server is having issues")
        assertThat(userError.actionText).isEqualTo("Try Again")
        assertThat(userError.icon).isEqualTo(ErrorIcon.ERROR)
    }
    
    @Test
    @DisplayName("HTTP 502 converts to Server error")
    fun `http 502 converts to server error`() {
        val exception = createHttpException(502)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("server is having issues")
    }
    
    @Test
    @DisplayName("HTTP 503 converts to Server error")
    fun `http 503 converts to server error`() {
        val exception = createHttpException(503)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("server is having issues")
    }
    
    @Test
    @DisplayName("HTTP 504 converts to Server error")
    fun `http 504 converts to server error`() {
        val exception = createHttpException(504)
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("server is having issues")
    }
    
    @Test
    @DisplayName("Unknown HTTP code converts to Server error with code")
    fun `unknown http code converts to server error with code`() {
        val exception = createHttpException(418) // I'm a teapot
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Server::class.java)
        assertThat(userError.message).contains("418")
    }
    
    @Test
    @DisplayName("FileNotFoundException converts to Storage error")
    fun `file not found exception converts to storage error`() {
        val exception = FileNotFoundException("/path/to/image.jpg")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Storage::class.java)
        assertThat(userError.message).contains("couldn't be found")
        assertThat(userError.actionText).isEqualTo("OK")
        assertThat(userError.icon).isEqualTo(ErrorIcon.STORAGE)
    }
    
    @Test
    @DisplayName("SecurityException converts to Storage error")
    fun `security exception converts to storage error`() {
        val exception = SecurityException("Permission denied")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Storage::class.java)
        assertThat(userError.message).contains("Permission denied")
        assertThat(userError.message).contains("allow")
    }
    
    @Test
    @DisplayName("Unknown exception converts to Unknown error")
    fun `unknown exception converts to unknown error`() {
        val exception = RuntimeException("Something unexpected")
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Unknown::class.java)
        assertThat(userError.title).isEqualTo("Something Went Wrong")
        assertThat(userError.message).contains("Something unexpected")
        assertThat(userError.actionText).isEqualTo("Dismiss")
        assertThat(userError.icon).isEqualTo(ErrorIcon.ERROR)
    }
    
    @Test
    @DisplayName("Exception with no message converts to Unknown error")
    fun `exception with no message converts to unknown error with default message`() {
        val exception = RuntimeException()
        val userError = exception.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Unknown::class.java)
        assertThat(userError.message).contains("unexpected error")
    }
    
    @Test
    @DisplayName("Result.Error converts to UserError")
    fun `result error converts to user error`() {
        val originalException = UnknownHostException("api.brickognize.com")
        val resultError = Result.Error(originalException, "Custom message")
        val userError = resultError.toUserError()
        
        assertThat(userError).isInstanceOf(UserError.Network::class.java)
        assertThat(userError.message).contains("No internet connection")
    }
    
    // Helper function to create HttpException for testing
    private fun createHttpException(code: Int): HttpException {
        val response = Response.error<Any>(
            code,
            okhttp3.ResponseBody.create(null, "")
        )
        return HttpException(response)
    }
}
