package com.studygenai.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.usecase.auth.SendOtpUseCase
import com.studygenai.domain.usecase.auth.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignUpUiState {
    object Idle : SignUpUiState()
    object Loading : SignUpUiState()
    data class AwaitingOtp(
        val email: String,
        val fullName: String,
        val password: String
    ) : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState

    var pendingOtp: String = ""
        private set
    var otpGeneratedAt: Long = 0L
        private set

    fun beginSignUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (password != confirmPassword) {
            _uiState.value = SignUpUiState.Error("Passwords do not match.")
            return
        }
        viewModelScope.launch {
            _uiState.value = SignUpUiState.Loading
            // Send OTP first — account is only created after OTP is verified
            sendOtpUseCase(toEmail = email, toName = fullName)
                .onSuccess { otp ->
                    pendingOtp = otp
                    otpGeneratedAt = System.currentTimeMillis()
                    _uiState.value = SignUpUiState.AwaitingOtp(
                        email    = email,
                        fullName = fullName,
                        password = password
                    )
                }
                .onFailure { e ->
                    _uiState.value = SignUpUiState.Error(
                        e.message ?: "Failed to send OTP."
                    )
                }
        }
    }

    fun clearError() { _uiState.value = SignUpUiState.Idle }
}