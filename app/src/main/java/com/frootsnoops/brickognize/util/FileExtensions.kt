package com.frootsnoops.brickognize.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Extension function to convert a File to a MultipartBody.Part for image upload.
 * 
 * @param partName The name of the multipart form field (e.g., "query_image")
 * @return A MultipartBody.Part ready to be sent via Retrofit
 */
fun File.toMultipartBody(partName: String = "query_image"): MultipartBody.Part {
    val requestBody = this.asRequestBody("image/*".toMediaTypeOrNull())
    return MultipartBody.Part.createFormData(partName, this.name, requestBody)
}
