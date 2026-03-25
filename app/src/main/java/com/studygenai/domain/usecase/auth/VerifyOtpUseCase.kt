package com.studygenai.domain.usecase.auth

import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor() {
    operator fun invoke(
        enteredOtp: String,
        actualOtp: String,
        generatedAt: Long,
        expiryMillis: Long = 5 * 60 * 1000L // 5 minutes
    ): Result<Unit> {
        val now = System.currentTimeMillis()
        return when {
            now - generatedAt > expiryMillis ->
                Result.failure(Exception("OTP has expired. Please request a new one."))
            enteredOtp.trim() != actualOtp.trim() ->
                Result.failure(Exception("Incorrect OTP. Please try again."))
            else -> Result.success(Unit)
        }
    }
}