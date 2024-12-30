package eu.thomaskuenneth.codescantoclipboard

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.MediaStore
import android.service.chooser.ChooserAction
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.DpSize
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory
import eu.thomaskuenneth.codescantoclipboard.screen.CodeScanToClipboardScreen
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL


private const val KEY_TEXT_ON_CLIPBOARD = "textOnClipboard"

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.calculateFromSize(DpSize.Zero) }

class CodeScanToClipboardActivity : ComponentActivity() {

    private lateinit var barcodeView: DecoratedBarcodeView
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var vibrator: Vibrator
    private lateinit var launcher: ActivityResultLauncher<PickVisualMediaRequest>

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                barcodeView.resume()
            }
        }

    // Keeping it as a simple variable to keep the ViewModel and ui state simple
    private var textOnClipboard = ""

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        savedInstanceState?.run {
            textOnClipboard = getString(KEY_TEXT_ON_CLIPBOARD, "")
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        clipboardManager = getSystemService(ClipboardManager::class.java)
        vibrator = getSystemService(Vibrator::class.java)
        val viewModel: CodeScanToClipboardViewModel by viewModels()

        launcher = registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) {
            it?.let { uri ->
                try {
                    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
                        ImageDecoder.decodeBitmap(
                            ImageDecoder.createSource(
                                contentResolver, uri
                            )
                        ) { decoder, _, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            decoder.isMutableRequired = true
                        }
                    } else {
                        @Suppress("DEPRECATION") MediaStore.Images.Media.getBitmap(
                            contentResolver, uri
                        )
                    }
                } catch (_: Exception) {
                    null
                }?.let { bitmap ->
                    viewModel.scanImageFile(bitmap = bitmap)
                }
            }
        }

        val root = layoutInflater.inflate(R.layout.layout, null) as DecoratedBarcodeView
        val formats = listOf(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39)
        barcodeView = root.findViewById(R.id.barcode_scanner)
        with(barcodeView) {
            decoderFactory = DefaultDecoderFactory(formats)
            initializeFromIntent(intent)
            val callback = object : BarcodeCallback {
                override fun barcodeResult(result: BarcodeResult) {
                    with(result.text) {
                        if (this == null || this == viewModel.scannerUiState.value.lastScannedText) {
                            return
                        }
                        viewModel.setLastScannedText(lastScannedText = this)
                        if (this.isNotEmpty()) viewModel.setShowScanImageFileError(
                            showScanFromFileError = false
                        )
                    }
                }
            }
            this.statusView.visibility = View.GONE
            cameraSettings.isAutoFocusEnabled = true
            cameraSettings.isContinuousFocusEnabled = true
            decodeContinuous(callback)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.scannerUiState.collect { state ->
                    if (state.flashOn) barcodeView.setTorchOn() else barcodeView.setTorchOff()
                    if (state.lastScannedText.isNotEmpty()) {
                        if (textOnClipboard != state.lastScannedText) {
                            vibrator.vibrate()
                            clipboardManager.copyToClipboard(state.lastScannedText)
                            textOnClipboard = state.lastScannedText
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bitmap.collect {
                    it?.run {
                        val imageUri = saveBitmapAndGetUri(this@CodeScanToClipboardActivity, it)
                        val chooserIntent2 = Intent.createChooser(Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_STREAM, imageUri)
                            type = "text/png"
                            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        }, null)
                        startActivity(chooserIntent2)
                        viewModel.clearBitmap()
                    }
                }
            }
        }

        setContent {
            CompositionLocalProvider(LocalWindowSizeClass provides calculateWindowSizeClass(this)) {
                CodeScanToClipboardScreen(
                    viewModel = viewModel,
                    root = root,
                    shareCallback = ::share,
                    scanImageFileCallback = ::scanImageFile
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_TEXT_ON_CLIPBOARD, textOnClipboard)
    }

    override fun onResume() {
        super.onResume()
        requestPermission.launch(Manifest.permission.CAMERA)
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    private fun ClipboardManager.copyToClipboard(text: String) {
        setPrimaryClip(ClipData.newPlainText("simple text", text))
    }

    private fun Vibrator.vibrate() {
        vibrate(VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun share(text: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = text.getMimeType()
        }

        val shareIntent = Intent.createChooser(sendIntent, null).apply {
            if (text.isValidURL() && VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val context = this@CodeScanToClipboardActivity
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(Intent.ACTION_VIEW, Uri.parse(text)),
                    PendingIntent.FLAG_IMMUTABLE
                )
                val customAction = ChooserAction.Builder(
                    Icon.createWithResource(
                        context, R.drawable.baseline_web_24
                    ), getString(R.string.open_browser), pendingIntent
                ).build()
                putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, arrayOf(customAction))
            }
        }
        startActivity(shareIntent)
    }

    private fun scanImageFile() {
        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
}

fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.externalCacheDir, "CodeScanToClipboard.png")
    FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 0, it) }
    return FileProvider.getUriForFile(
        context, "eu.thomaskuenneth.codescantoclipboard.fileprovider", file
    )
}

private fun String.getMimeType(): String =
    if (this.isValidURL() && (VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE)) "text/uri-list" else "text/plain"

private fun String.isValidURL(): Boolean = try {
    URL(this).toURI()
    true
} catch (_: Exception) {
    false
}
