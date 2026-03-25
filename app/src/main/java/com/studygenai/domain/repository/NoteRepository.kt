package com.studygenai.domain.repository

import com.studygenai.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    suspend fun uploadNote(title: String, subject: String, imageUriString: String): Result<Note>
    fun getAllNotes(): Flow<List<Note>>
    suspend fun getNoteById(noteId: String): Note?
    suspend fun deleteNote(noteId: String): Result<Unit>
    fun searchNotes(query: String): Flow<List<Note>>
}
