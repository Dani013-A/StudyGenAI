package com.studygenai.data.firebase

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Saves image from URI to app's internal storage
    // Returns the absolute local file path string to store in Firestore
    fun saveImageLocally(uri: Uri): Result<String> {
        return try {
            val fileName = "note_${UUID.randomUUID()}.jpg"
            val destFile = File(context.filesDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return Result.failure(Exception("Could not open image stream"))

            Result.success(destFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Deletes a locally stored image when a note is deleted
    fun deleteImageLocally(filePath: String): Result<Unit> {
        return try {
            val file = File(filePath)
            if (file.exists()) file.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}