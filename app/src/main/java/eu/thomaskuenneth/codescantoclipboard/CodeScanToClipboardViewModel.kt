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
    val showScannerActions: Boolean = false,
    // This is pretty specific and can be generalized if we need to show other errors
    val showScanImageFileError: Boolean = false,
)

data class ScannerUiState(
    val lastScannedText: String = "",
    val flashOn: Boolean = false,
)

data class GeneratorUiState(
    val width: String = "400",
    val height: String = "400",
    val code: String = "",
)

class CodeScanToClipboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CodeScanToClipboardUiState())
    val uiState: StateFlow<CodeScanToClipboardUiState> = _uiState.asStateFlow()

    private val _scannerUiState = MutableStateFlow(ScannerUiState())
    val scannerUiState: StateFlow<ScannerUiState> = _scannerUiState.asStateFlow()

    private val _generatorUiState = MutableStateFlow(GeneratorUiState())
    val generatorUiState: StateFlow<GeneratorUiState> = _generatorUiState.asStateFlow()

    fun setShowScannerActions(showActions: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(showScannerActions = showActions)
        }
    }

    fun setLastScannedText(lastScannedText: String) {
        _scannerUiState.update { currentState ->
            currentState.copy(lastScannedText = lastScannedText)
        }
    }

    fun clearLastScannedText() {
        _scannerUiState.update { currentState ->
            currentState.copy(lastScannedText = "")
        }
    }

    fun toggleFlash() {
        _scannerUiState.update { currentState ->
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
                _scannerUiState.update { currentState ->
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
        _generatorUiState.update { currentState ->
            currentState.copy(width = width)
        }
    }

    fun setHeight(height: String) {
        _generatorUiState.update { currentState ->
            currentState.copy(height = height)
        }
    }

    fun setCode(code: String) {
        _generatorUiState.update { currentState ->
            currentState.copy(code = code)
        }
    }

    fun isWidthError() = with(_generatorUiState.value.width) { isNotInRange() }
    fun isHeightError() = with(_generatorUiState.value.height) { isNotInRange() }
    private fun isCodeError() = with(_generatorUiState.value.code) { isEmpty() }
    private fun String.isNotInRange() = isEmpty() || !isDigitsOnly() ||
            with(toInt()) { this !in 200..800 }

    fun canGenerate() = !isWidthError() && !isHeightError() && !isCodeError()

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    fun generate() {
        val barcodeEncoder = BarcodeEncoder()
        with(_generatorUiState.value) {
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
