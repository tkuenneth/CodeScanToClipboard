package eu.thomaskuenneth.codescantoclipboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CodeScanToClipboardUiState(
    val lastScannedText: String = "",
    val flashOn: Boolean = false
)

class CodeScanToClipboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CodeScanToClipboardUiState())
    val uiState: StateFlow<CodeScanToClipboardUiState> = _uiState.asStateFlow()

    fun setLastScannedText(lastScannedText: String) {
        _uiState.update { currentState ->
            currentState.copy(lastScannedText = lastScannedText)
        }
    }

    fun clearLastScannedText() {
        _uiState.update { currentState ->
            currentState.copy(lastScannedText = "")
        }
    }

    fun toggleFlash() {
        _uiState.update { currentState ->
            currentState.copy(flashOn = !currentState.flashOn)
        }
    }
}
