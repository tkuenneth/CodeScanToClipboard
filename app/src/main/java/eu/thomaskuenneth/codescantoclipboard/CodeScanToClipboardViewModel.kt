package eu.thomaskuenneth.codescantoclipboard

import android.graphics.Bitmap
import androidx.core.graphics.scale
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.common.HybridBinarizer
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Hashtable

data class CodeScanToClipboardUiState(
    // Scanner
    val lastScannedText: String = "",
    val flashOn: Boolean = false,

    // Generator
    val width: String = "400",
    val height: String = "400",
    val code: String = "",

    // General
    val showActions: Boolean = false,
    // This is pretty specific and can be generalized if we need to show other errors
    val showScanImageFileError: Boolean = false,
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

    fun scanImageFile(bitmap: Bitmap) {
        viewModelScope.launch {
            val reader = MultiFormatReader().also { reader ->
                reader.setHints(Hashtable<DecodeHintType, Any>().also { hints ->
                    hints[DecodeHintType.TRY_HARDER] = java.lang.Boolean.TRUE
                })
            }
            val imageFileWidth = bitmap.width
            val imageFileHeight = bitmap.height
            val work = if (imageFileWidth > 1000 && imageFileHeight > 1000) {
                val ratio = imageFileWidth.toFloat() / imageFileHeight.toFloat()
                val scaledBitmap = bitmap.scale(width = 1000, height = (1000F / ratio).toInt())
                bitmap.recycle()
                scaledBitmap
            } else {
                bitmap
            }
            val pixels = IntArray(work.width * work.height)
            work.getPixels(pixels, 0, work.width, 0, 0, work.width, work.height)
            val source = RGBLuminanceSource(work.width, work.height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val rawResult = reader.decodeWithState(binaryBitmap)
                _uiState.update { currentState ->
                    currentState.copy(lastScannedText = rawResult.text)
                }
            } catch (_: ReaderException) {
                clearLastScannedText()
                setShowScanImageFileError(showScanFromFileError = true)
            } finally {
                work.recycle()
                reader.reset()
            }
        }
    }

    fun setShowScanImageFileError(showScanFromFileError: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showScanImageFileError = showScanFromFileError)
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
