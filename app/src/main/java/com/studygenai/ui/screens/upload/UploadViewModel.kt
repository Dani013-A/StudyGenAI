package com.studygenai.ui.screens.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studygenai.ai.ocr.OcrEngine
import com.studygenai.ai.ocr.OcrResult
import com.studygenai.domain.repository.NoteRepository
import com.studygenai.domain.usecase.note.SaveScannedNoteUseCase
import com.studygenai.utils.ImageUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val noteRepository: NoteRepository,
    private val ocrEngine: OcrEngine,
    private val saveScannedNoteUseCase: SaveScannedNoteUseCase
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()

    private val _scannedText = MutableStateFlow<String?>(null)
    val scannedText = _scannedText.asStateFlow()

    fun uploadNote(title: String, subject: String, fileUriString: String, isPdf: Boolean) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            val result = noteRepository.uploadNote(title, subject, fileUriString, isPdf)
            if (result.isSuccess) {
                _uploadState.value = UploadState.Success
            } else {
                _uploadState.value = UploadState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun extractTextFromImage(uri: Uri) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            try {
                val bitmap = ImageUtils.uriToBitmap(uri)
                if (bitmap != null) {
                    when (val result = ocrEngine.extractText(bitmap)) {
                        is OcrResult.Success -> {
                            _scannedText.value = result.text
                            _uploadState.value = UploadState.Idle // Done loading
                        }
                        is OcrResult.Failure -> {
                            _uploadState.value = UploadState.Error(result.error.message ?: "OCR Failed")
                        }
                    }
                } else {
                    _uploadState.value = UploadState.Error("Could not load image")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error(e.message ?: "Error extracting text")
            }
        }
    }

    fun saveScannedNote(title: String, subject: String, text: String, uri: Uri?) {
        _uploadState.value = UploadState.Loading
        viewModelScope.launch {
            val uriString = uri?.toString()
            val result = saveScannedNoteUseCase(title, subject, text, uriString)
            if (result.isSuccess) {
                _uploadState.value = UploadState.Success
            } else {
                _uploadState.value = UploadState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateScannedText(text: String?) {
        _scannedText.value = text
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
        _scannedText.value = null
    }
}

