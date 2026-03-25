package com.studygenai.domain.usecase.summary

import com.studygenai.domain.model.Summary
import com.studygenai.domain.repository.NoteRepository

// TODO: Fetches note text from repo, calls GeminiClient via SummaryRepository
class GenerateSummaryUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: String): Result<Summary> {
        // Implementation goes here
        TODO("Wire up GeminiClient for summary generation")
    }
}
