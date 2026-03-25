package com.studygenai.domain.model

data class QuizQuestion(
    val question: String = "",
    val options: List<String> = emptyList(), // empty for Identification type
    val answer: String = "",
    val type: String = "" // "mcq", "truefalse", "identification"
)
