package com.studygenai.domain.usecase.auth

import com.studygenai.data.firebase.EmailJSService
import javax.inject.Inject

class SendOtpUseCase @Inject constructor(
    private val emailJSService: EmailJSService
) {
    // Generates a 6-digit OTP, sends it via EmailJS, returns the OTP
    // so the ViewModel can store it in memory for comparison
    suspend operator fun invoke(
        toEmail: String,
        toName: String
    ): Result<String> {
        val otp = (100000..999999).random().toString()
        return emailJSService.sendOtp(
            toEmail  = toEmail,
            toName   = toName,
            otpCode  = otp
        ).map { otp } // return the otp on success so ViewModel can hold it
    }
}