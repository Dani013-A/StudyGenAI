package com.studygenai.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.AuthRepository
import com.studygenai.domain.repository.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val userName: String = "",
    val recentNotes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadUserName()
        observeRecentNotes()
    }

    private fun loadUserName() {
        val user = authRepository.getCurrentUser()
        _uiState.update { it.copy(userName = user?.fullName ?: "") }
    }

    private fun observeRecentNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .catch { /* handle silently */ }
                .collect { notes ->
                    _uiState.update {
                        it.copy(recentNotes = notes.take(5))
                    }
                }
        }
    }
}