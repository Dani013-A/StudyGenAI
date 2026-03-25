package com.studygenai.domain.usecase.note

import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetAllNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getAllNotes()
}
