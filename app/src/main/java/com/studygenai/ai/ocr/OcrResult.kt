package com.studygenai.ai.ocr

sealed class OcrResult {
    data class Success(val text: String) : OcrResult()
    data class Failure(val error: Throwable) : OcrResult()
}
