package com.studygenai.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.studygenai.ai.ocr.OcrEngine
import com.studygenai.ai.ocr.OcrResult
import com.studygenai.data.firebase.StorageService
import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.NoteRepository
import com.studygenai.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageService: StorageService,
    private val ocrEngine: OcrEngine,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : NoteRepository {

    private fun notesCollection() = firestore
        .collection(Constants.COLLECTION_USERS)
        .document(auth.currentUser?.uid ?: "")
        .collection(Constants.COLLECTION_NOTES)

    override suspend fun uploadNote(
        title: String,
        subject: String,
        fileUriString: String,
        isPdf: Boolean
    ): Result<Note> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            val uri = Uri.parse(fileUriString)

            // Step 1 — save file locally
            val saveResult = storageService.saveImageLocally(uri)
            if (saveResult.isFailure) {
                return@withContext Result.failure(saveResult.exceptionOrNull()
                    ?: Exception("Failed to save file"))
            }
            val localPath = saveResult.getOrThrow()

            // Step 2 — run OCR
            var rawText = ""
            if (isPdf) {
                val bitmaps = com.studygenai.utils.PdfUtils.getBitmapsFromPdfUri(context, uri)
                val textBuilder = StringBuilder()
                for (bitmap in bitmaps) {
                    when (val ocrResult = ocrEngine.extractText(bitmap)) {
                        is OcrResult.Success -> textBuilder.append(ocrResult.text).append("\n")
                        is OcrResult.Failure -> {}
                    }
                }
                rawText = textBuilder.toString()
            } else {
                val bitmap = com.studygenai.utils.ImageUtils.uriToBitmap(uri)
                if (bitmap != null) {
                    when (val ocrResult = ocrEngine.extractText(bitmap)) {
                        is OcrResult.Success -> rawText = ocrResult.text
                        is OcrResult.Failure -> {}
                    }
                }
            }

            // Step 3 — save to Firestore
            val docRef = notesCollection().document()
            val note = Note(
                id        = docRef.id,
                title     = title,
                subject   = subject,
                rawText   = rawText,
                imageUrl  = localPath,
                createdAt = System.currentTimeMillis()
            )
            val data = mapOf(
                "id"        to note.id,
                "title"     to note.title,
                "subject"   to note.subject,
                "rawText"   to note.rawText,
                "imageUrl"  to note.imageUrl,
                "createdAt" to note.createdAt
            )
            // Remove .await() because Firestore writes pend until network sync.
            // Using .await() causes it to hang indefinitely if there is no strong network connection.
            docRef.set(data)
            
            Result.success(note)
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Unknown error"))
        }
    }

    override fun getAllNotes(): Flow<List<Note>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = notesCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val notes = snapshot.documents.mapNotNull { doc ->
                    try {
                        Note(
                            id        = doc.getString("id") ?: doc.id,
                            title     = doc.getString("title") ?: "",
                            subject   = doc.getString("subject") ?: "",
                            rawText   = doc.getString("rawText") ?: "",
                            imageUrl  = doc.getString("imageUrl") ?: "",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                }
                trySend(notes)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun getNoteById(noteId: String): Note? {
        return try {
            val doc = notesCollection().document(noteId).get().await()
            Note(
                id        = doc.getString("id") ?: doc.id,
                title     = doc.getString("title") ?: "",
                subject   = doc.getString("subject") ?: "",
                rawText   = doc.getString("rawText") ?: "",
                imageUrl  = doc.getString("imageUrl") ?: "",
                createdAt = doc.getLong("createdAt") ?: 0L
            )
        } catch (e: Exception) { null }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit> {
        return try {
            // Delete local image file first
            val doc = notesCollection().document(noteId).get().await()
            val localPath = doc.getString("imageUrl") ?: ""
            if (localPath.isNotEmpty()) {
                storageService.deleteImageLocally(localPath)
            }
            notesCollection().document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> = flow {
        try {
            val snapshot = notesCollection().get().await()
            val notes = snapshot.documents.mapNotNull { doc ->
                try {
                    Note(
                        id        = doc.getString("id") ?: doc.id,
                        title     = doc.getString("title") ?: "",
                        subject   = doc.getString("subject") ?: "",
                        rawText   = doc.getString("rawText") ?: "",
                        imageUrl  = doc.getString("imageUrl") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } catch (e: Exception) { null }
            }.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.subject.contains(query, ignoreCase = true)
            }
            emit(notes)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun saveScannedNote(
        title: String,
        subject: String,
        rawText: String,
        imageUriString: String?
    ): Result<Note> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        try {
            var localPath = ""

            // Save image locally if provided
            if (!imageUriString.isNullOrEmpty()) {
                val uri = Uri.parse(imageUriString)
                val saveResult = storageService.saveImageLocally(uri)
                if (saveResult.isSuccess) {
                    localPath = saveResult.getOrThrow()
                }
            }

            // Save to Firestore
            val docRef = notesCollection().document()
            val note = Note(
                id        = docRef.id,
                title     = title,
                subject   = subject,
                rawText   = rawText,
                imageUrl  = localPath,
                createdAt = System.currentTimeMillis()
            )
            val data = mapOf(
                "id"        to note.id,
                "title"     to note.title,
                "subject"   to note.subject,
                "rawText"   to note.rawText,
                "imageUrl"  to note.imageUrl,
                "createdAt" to note.createdAt
            )
            docRef.set(data)
            
            Result.success(note)
        } catch (e: Throwable) {
            Result.failure(Exception(e.message ?: "Unknown error"))
        }
    }
}