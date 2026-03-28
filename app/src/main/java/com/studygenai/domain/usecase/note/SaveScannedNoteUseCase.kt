package com.studygenai.domain.usecase.note

import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.NoteRepository
import javax.inject.Inject

class SaveScannedNoteUseCase @Inject constructor(private val repository: NoteRepository) {
    suspend operator fun invoke(
        title: String,
        subject: String,
        rawText: String,
        imageUriString: String?
    ): Result<Note> {
        return repository.saveScannedNote(title, subject, rawText, imageUriString)
    }
}
