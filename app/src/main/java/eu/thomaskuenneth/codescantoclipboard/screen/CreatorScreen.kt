package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
    viewModel.setShowScannerActions(showActions = false)
    val state by viewModel.generatorUiState.collectAsState()
    with(state) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(all = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MyTextField(value = width,
                resId = R.string.width_in_pixel,
                message = if (viewModel.isWidthError()) stringResource(id = R.string.range_hint) else "",
                onValueChange = { viewModel.setWidth(it) })
            MyTextField(value = height,
                resId = R.string.height_in_pixel,
                message = if (viewModel.isHeightError()) stringResource(id = R.string.range_hint) else "",
                onValueChange = { viewModel.setHeight(it) })
            MyTextField(
                value = code,
                resId = R.string.code,
                message = if (code.isEmpty()) stringResource(id = R.string.cannot_be_empty) else "",
                onValueChange = { viewModel.setCode(it) },
                keyboardType = KeyboardType.Ascii
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.generate() }, enabled = viewModel.canGenerate()
            ) {
                Text(text = stringResource(id = R.string.generate))
            }
        }
    }
}

@Composable
fun MyTextField(
    value: String,
    @StringRes resId: Int,
    onValueChange: (String) -> Unit,
    message: String = "",
    keyboardType: KeyboardType = KeyboardType.Number
) {
    OutlinedTextField(
        value = value,
        singleLine = true,
        label = {
            Text(text = stringResource(id = resId))
        },
        isError = message.isNotEmpty(),
        supportingText = { Text(text = message) },
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}
