package com.studygenai.domain.usecase.note

import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.NoteRepository

// TODO: Called by UploadViewModel
// Input: title, subject, image URI string
// Output: Result<Note>
class UploadNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(title: String, subject: String, imageUri: String): Result<Note> {
        return repository.uploadNote(title, subject, imageUri)
    }
}
