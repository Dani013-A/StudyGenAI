package com.studygenai.domain.repository

import com.studygenai.domain.model.Flashcard
import kotlinx.coroutines.flow.Flow

interface FlashcardRepository {
    suspend fun generateFlashcards(noteId: String): Result<List<Flashcard>>
    fun getFlashcardsByNote(noteId: String): Flow<List<Flashcard>>
}
