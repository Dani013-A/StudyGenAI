package com.studygenai.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.domain.model.Note
import com.studygenai.domain.repository.NoteRepository
import com.studygenai.domain.usecase.note.DeleteNoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val allNotes: List<Note> = emptyList(),
    val filteredNotes: List<Note> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val deleteSuccess: Boolean = false
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val deleteNoteUseCase: DeleteNoteUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState

    init {
        observeNotes()
    }

    private fun observeNotes() {
        viewModelScope.launch {
            noteRepository.getAllNotes()
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message) }
                }
                .collect { notes ->
                    _uiState.update { state ->
                        state.copy(
                            allNotes      = notes,
                            filteredNotes = filterNotes(notes, state.searchQuery)
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery   = query,
                filteredNotes = filterNotes(state.allNotes, query)
            )
        }
    }

    private fun filterNotes(notes: List<Note>, query: String): List<Note> {
        if (query.isBlank()) return notes
        return notes.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.subject.contains(query, ignoreCase = true)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            deleteNoteUseCase(noteId)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, deleteSuccess = true) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message)
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, deleteSuccess = false) }
    }
}