package com.studygenai.domain.model

data class Quiz(
    val id: String = "",
    val noteId: String = "",
    val questions: List<QuizQuestion> = emptyList(),
    val score: Int = 0,
    val total: Int = 0,
    val quizType: String = "",
    val takenAt: Long = 0L
)
