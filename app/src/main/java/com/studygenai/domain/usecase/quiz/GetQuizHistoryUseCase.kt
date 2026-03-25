package com.studygenai.domain.usecase.quiz

import com.studygenai.domain.model.Quiz
import com.studygenai.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow

class GetQuizHistoryUseCase(private val repository: QuizRepository) {
    operator fun invoke(): Flow<List<Quiz>> = repository.getQuizHistory()
}
