package eu.thomaskuenneth.codescantoclipboard

import android.graphics.Bitmap
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


data class CodeScanToClipboardUiState(
    // Scanner
    val lastScannedText: String = "",
    val flashOn: Boolean = false,

    // Generator
    val width: String = "400",
    val height: String = "400",
    val code: String = "",

    // app bar
    val showActions: Boolean = false
)

class CodeScanToClipboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CodeScanToClipboardUiState())
    val uiState: StateFlow<CodeScanToClipboardUiState> = _uiState.asStateFlow()

    fun setShowActions(showActions: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showActions = showActions)
        }
    }

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

    // Creator

    fun setWidth(width: String) {
        _uiState.update { currentState ->
            currentState.copy(width = width)
        }
    }

    fun setHeight(height: String) {
        _uiState.update { currentState ->
            currentState.copy(height = height)
        }
    }

    fun setCode(code: String) {
        _uiState.update { currentState ->
            currentState.copy(code = code)
        }
    }

    fun isWidthError() = with(_uiState.value.width) { isNotInRange() }
    fun isHeightError() = with(_uiState.value.height) { isNotInRange() }
    private fun isCodeError() = with(_uiState.value.code) { isEmpty() }
    private fun String.isNotInRange() = isEmpty() || !isDigitsOnly() ||
            with(toInt()) { this !in 200..800 }

    fun canGenerate() = !isWidthError() && !isHeightError() && !isCodeError()

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    fun generate() {
        val barcodeEncoder = BarcodeEncoder()
        with(_uiState.value) {
            try {
                _bitmap.value = barcodeEncoder.encodeBitmap(
                    code,
                    BarcodeFormat.QR_CODE,
                    width.toInt(),
                    height.toInt()
                )
            } catch (ex: Exception) {
                // ignore
            }
        }
    }
}
