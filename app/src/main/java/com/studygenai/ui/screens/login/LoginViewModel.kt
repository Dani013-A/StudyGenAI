package com.studygenai.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.usecase.auth.SendOtpUseCase
import com.studygenai.domain.usecase.auth.SignInUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    object Idle : LoginUiState()
    object LoadingSignIn : LoginUiState()
    object LoadingSendOtp : LoginUiState()
    // Credentials verified — now show OTP screen
    data class AwaitingOtp(val email: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val sendOtpUseCase: SendOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    // Held in memory only — never persisted
    var pendingOtp: String = ""
        private set
    var otpGeneratedAt: Long = 0L
        private set

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.LoadingSignIn
            signInUseCase(email, password)
                .onSuccess { user ->
                    // Credentials correct — now send OTP
                    _uiState.value = LoginUiState.LoadingSendOtp
                    sendOtpUseCase(toEmail = email, toName = user.fullName.ifBlank { "Student" })
                        .onSuccess { otp ->
                            pendingOtp = otp
                            otpGeneratedAt = System.currentTimeMillis()
                            _uiState.value = LoginUiState.AwaitingOtp(email)
                        }
                        .onFailure { e ->
                            _uiState.value = LoginUiState.Error(
                                e.message ?: "Failed to send OTP."
                            )
                        }
                }
                .onFailure { e ->
                    _uiState.value = LoginUiState.Error(
                        e.message ?: "Invalid email or password."
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = LoginUiState.Idle
    }
}