package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel

@Composable
fun CreatorScreen(
    viewModel: CodeScanToClipboardViewModel,
) {
    val state by viewModel.uiState.collectAsState()
}
