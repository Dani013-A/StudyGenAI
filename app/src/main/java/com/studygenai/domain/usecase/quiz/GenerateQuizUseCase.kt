package com.studygenai.domain.usecase.quiz

import com.studygenai.domain.model.QuizQuestion
import com.studygenai.domain.repository.QuizRepository

class GenerateQuizUseCase(private val repository: QuizRepository) {
    suspend operator fun invoke(noteId: String, count: Int, type: String): Result<List<QuizQuestion>> {
        return repository.generateQuiz(noteId, count, type)
    }
}
