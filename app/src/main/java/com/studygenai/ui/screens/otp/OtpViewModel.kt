package com.studygenai.ui.screens.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.domain.usecase.auth.SendOtpUseCase
import com.studygenai.domain.usecase.auth.SignUpUseCase
import com.studygenai.domain.usecase.auth.VerifyOtpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OtpUiState {
    object Idle : OtpUiState()
    object Loading : OtpUiState()
    object ResendingOtp : OtpUiState()
    object NavigateToDashboard : OtpUiState()
    object NavigateToOnboarding : OtpUiState()
    data class Error(val message: String) : OtpUiState()
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OtpUiState>(OtpUiState.Idle)
    val uiState: StateFlow<OtpUiState> = _uiState

    var pendingOtp: String = ""
    var otpGeneratedAt: Long = 0L

    fun verifyAndProceed(
        enteredOtp: String,
        flow: String,
        email: String,
        fullName: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value = OtpUiState.Loading
            android.util.Log.d("OTP_DEBUG", "Step 1: Verifying OTP")
            android.util.Log.d("OTP_DEBUG", "Entered: '$enteredOtp' | Actual: '$pendingOtp'")

            // Step 1 — verify OTP locally
            val verifyResult = verifyOtpUseCase(
                enteredOtp  = enteredOtp,
                actualOtp   = pendingOtp,
                generatedAt = otpGeneratedAt
            )

            if (verifyResult.isFailure) {
                android.util.Log.e("OTP_DEBUG", "OTP verify failed: ${verifyResult.exceptionOrNull()?.message}")
                _uiState.value = OtpUiState.Error(
                    verifyResult.exceptionOrNull()?.message ?: "Incorrect OTP."
                )
                return@launch
            }

            android.util.Log.d("OTP_DEBUG", "Step 2: OTP verified. Flow = $flow")

            // Step 2 — if signup, create the account
            if (flow == "signup") {
                android.util.Log.d("OTP_DEBUG", "Step 2a: Creating account for $email")
                val signUpResult = signUpUseCase(fullName, email, password)
                if (signUpResult.isFailure) {
                    android.util.Log.e("OTP_DEBUG", "SignUp failed: ${signUpResult.exceptionOrNull()?.message}")
                    _uiState.value = OtpUiState.Error(
                        signUpResult.exceptionOrNull()?.message ?: "Failed to create account."
                    )
                    return@launch
                }
                android.util.Log.d("OTP_DEBUG", "Step 2b: Account created successfully")
            }

            // Step 3 — get current user
            android.util.Log.d("OTP_DEBUG", "Step 3: Getting current user")
            val currentUser = authRepository.getCurrentUser()
            android.util.Log.d("OTP_DEBUG", "Current user: $currentUser")

            val uid = currentUser?.uid ?: ""
            android.util.Log.d("OTP_DEBUG", "UID: '$uid'")

            if (uid.isEmpty()) {
                android.util.Log.e("OTP_DEBUG", "UID is empty — auth state not updated yet")
                _uiState.value = OtpUiState.NavigateToOnboarding
                return@launch
            }

            // Step 4 — check onboarding
            android.util.Log.d("OTP_DEBUG", "Step 4: Checking isOnboarded for uid: $uid")
            val onboarded = authRepository.isOnboarded(uid)
            android.util.Log.d("OTP_DEBUG", "isOnboarded result: $onboarded")

            _uiState.value = if (onboarded) {
                OtpUiState.NavigateToDashboard
            } else {
                OtpUiState.NavigateToOnboarding
            }
        }
    }

    fun resendOtp(email: String, name: String) {
        viewModelScope.launch {
            _uiState.value = OtpUiState.ResendingOtp
            sendOtpUseCase(toEmail = email, toName = name.ifBlank { "Student" })
                .onSuccess { otp ->
                    pendingOtp = otp
                    otpGeneratedAt = System.currentTimeMillis()
                    _uiState.value = OtpUiState.Idle
                }
                .onFailure { e ->
                    _uiState.value = OtpUiState.Error(
                        e.message ?: "Failed to resend OTP."
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = OtpUiState.Idle
    }
}