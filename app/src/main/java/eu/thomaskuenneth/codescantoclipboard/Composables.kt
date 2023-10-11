package eu.thomaskuenneth.codescantoclipboard

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScanToClipboardScreen(
    viewModel: CodeScanToClipboardViewModel,
    root: View,
    shareCallback: (String) -> Unit
) {
    MaterialTheme(
        colorScheme = defaultColorScheme()
    ) {
        val state by viewModel.uiState.collectAsState()
        with(state) {
            Scaffold(topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.app_name)
                        )
                    },
                    actions = {
                        IconButtonWithTooltip(
                            onClick = { viewModel.toggleFlash() },
                            painter = painterResource(id = if (flashOn) R.drawable.baseline_flash_on_24 else R.drawable.baseline_flash_off_24),
                            contentDescription = stringResource(id = if (flashOn) R.string.flash_on else R.string.flash_off)
                        )
                        if (lastScannedText.isNotEmpty()) {
                            IconButtonWithTooltip(
                                onClick = { shareCallback(lastScannedText) },
                                painter = painterResource(id = R.drawable.baseline_share_24),
                                contentDescription = stringResource(id = R.string.share)
                            )
                            IconButtonWithTooltip(
                                onClick = { viewModel.clearLastScannedText() },
                                painter = painterResource(id = R.drawable.baseline_clear_24),
                                contentDescription = stringResource(id = R.string.clear)
                            )
                        }
                    }
                )
            }) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = it),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(modifier = Modifier.fillMaxSize(), factory = {
                        root
                    })
                    if (lastScannedText.isNotBlank()) {
                        Text(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.8F
                                    ), shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            text = lastScannedText,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    onClick: () -> Unit,
    painter: Painter,
    contentDescription: String
) {
    PlainTooltipBox(tooltip = {
        Text(text = contentDescription)
    }) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.tooltipAnchor()
        ) {
            Icon(
                painter = painter,
                contentDescription = contentDescription
            )
        }
    }
}
