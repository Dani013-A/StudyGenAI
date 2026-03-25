package com.studygenai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.model.User
import com.studygenai.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext

data class SettingsUiState(
    val user: User = User(),
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isSigningOut: Boolean = false,
    val signedOut: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences("studygenai_prefs", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(SettingsUiState(isDarkMode = prefs.getBoolean("dark_mode", false)))
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: User()

            // Fetch full name from Firestore
            val fullUser = try {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val doc = firestore
                    .collection("users")
                    .document(user.uid)
                    .get()
                    .await()
                User(
                    uid      = user.uid,
                    fullName = doc.getString("fullName") ?: "",
                    email    = doc.getString("email") ?: user.email
                )
            } catch (e: Exception) {
                user
            }
            _uiState.update { it.copy(user = fullUser) }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSigningOut = true) }
            authRepository.signOut()
            _uiState.update { it.copy(isSigningOut = false, signedOut = true) }
        }
    }
}
