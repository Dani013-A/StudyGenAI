package com.studygenai.utils

object Constants {
    const val GEMINI_MODEL        = "gemini-1.5-flash"
    const val MAX_TOKENS          = 2048
    const val MAX_QUIZ_QUESTIONS  = 20

    // Firestore collection paths
    const val COLLECTION_USERS      = "users"
    const val COLLECTION_NOTES      = "notes"
    const val COLLECTION_QUIZZES    = "quizzes"
    const val COLLECTION_FLASHCARDS = "flashcards"

    // Quiz types
    const val QUIZ_MCQ            = "mcq"
    const val QUIZ_TRUE_FALSE     = "truefalse"
    const val QUIZ_IDENTIFICATION = "identification"
}