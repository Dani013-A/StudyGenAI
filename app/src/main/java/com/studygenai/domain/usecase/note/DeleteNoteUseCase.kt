package com.studygenai.domain.usecase.note

import com.studygenai.domain.repository.NoteRepository

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: String): Result<Unit> {
        return repository.deleteNote(noteId)
    }
}
