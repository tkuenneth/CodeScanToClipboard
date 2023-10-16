package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel
import eu.thomaskuenneth.codescantoclipboard.R

@Composable
fun CreatorScreen(
    viewModel: CodeScanToClipboardViewModel,
) {
    val state by viewModel.uiState.collectAsState()
    with(state) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = width,
                singleLine = true,
                label = {
                    Text(text = stringResource(id = R.string.width_in_pixel))
                },
                onValueChange = {
                    viewModel.setWidth(it)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = height,
                singleLine = true,
                label = {
                    Text(text = stringResource(id = R.string.height_in_pixel))
                },
                onValueChange = {
                    viewModel.setHeight(it)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = code,
                singleLine = true,
                label = {
                    Text(text = stringResource(id = R.string.code))
                },
                onValueChange = {
                    viewModel.setCode(it)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.generate() },
                enabled = viewModel.canGenerate()
            ) {
                Text(text = stringResource(id = R.string.generate))
            }
        }
    }
}
