package eu.thomaskuenneth.codescantoclipboard

import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScanToClipboardScreen(root: View, value: String) {
    MaterialTheme(
        colorScheme = defaultColorScheme()
    ) {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text(
                    text = stringResource(id = R.string.app_name)
                )
            })
        }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = it),
                contentAlignment = Alignment.TopCenter
            ) {
                AndroidView(modifier = Modifier.fillMaxSize(), factory = {
                    root
                })
                if (value.isNotBlank()) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = value,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
