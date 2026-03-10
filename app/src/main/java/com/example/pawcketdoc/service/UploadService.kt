package com.example.pawcketdoc.service

import android.content.Context
import android.net.Uri
import com.cloudinary.utils.ObjectUtils
import com.example.pawcketdoc.config.CloudinaryConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class UploadService(private val context: Context) {

    suspend fun uploadPetImage(
        uri: Uri,
        folder: String = "pawcketdoc/"
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val stream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        tempFile.outputStream().use { stream?.copyTo(it) }

        val result = CloudinaryConfig.instance.uploader().upload(
            tempFile.absolutePath,
            ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", folder
            )
        )

        mapOf(
            "secure_url" to result["secure_url"] as String,
            "public_id" to result["public_id"] as String
        )
    }

    suspend fun uploadDocument(
        uri: Uri,
        folder: String = "pawcketdoc/documents/"
    ): Map<String, String> = withContext(Dispatchers.IO) {
        val mimeType = context.contentResolver.getType(uri)
        val extension = when {
            mimeType == "application/pdf" -> ".pdf"
            mimeType?.startsWith("image/") == true -> ".jpg"
            else -> ".jpg"
        }

        val stream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("doc_upload_", extension, context.cacheDir)
        tempFile.outputStream().use { stream?.copyTo(it) }

        val result = CloudinaryConfig.instance.uploader().upload(
            tempFile.absolutePath,
            ObjectUtils.asMap(
                "resource_type", "auto",
                "folder", folder
            )
        )

        mapOf(
            "secure_url" to result["secure_url"] as String,
            "public_id" to result["public_id"] as String
        )
    }
}