package com.studygenai.domain.repository

import com.studygenai.domain.model.Quiz
import com.studygenai.domain.model.QuizQuestion
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun generateQuiz(noteId: String, count: Int, type: String): Result<List<QuizQuestion>>
    suspend fun saveQuizResult(quiz: Quiz): Result<Unit>
    fun getQuizHistory(): Flow<List<Quiz>>
}
