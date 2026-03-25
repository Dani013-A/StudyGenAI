package com.studygenai.ai.gemini

sealed class GeminiResult {
    data class Success(val text: String) : GeminiResult()
    data class Failure(val error: Throwable) : GeminiResult()
}
