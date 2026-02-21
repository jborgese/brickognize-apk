package com.frootsnoops.brickognize.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface UriFileConverter {
    fun uriToFile(uri: Uri): File
}

@Singleton
class UriFileConverterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UriFileConverter {
    
    override fun uriToFile(uri: Uri): File {
        val tempFile = File(context.cacheDir, "temp_scan_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }
}
