package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel
import eu.thomaskuenneth.codescantoclipboard.LocalWindowSizeClass
import eu.thomaskuenneth.codescantoclipboard.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorScreen(
    viewModel: CodeScanToClipboardViewModel,
) {
    viewModel.setShowScannerActions(showActions = false)
    val state by viewModel.generatorUiState.collectAsState()
    val options = listOf(
        stringResource(id = R.string.qrcode),
        stringResource(id = R.string.ean13),
    )
    val callback = { viewModel.setGeneratorExceptionMessage("") }
    with(state) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(all = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val widthAndHeight: @Composable () -> Unit = {
                    ValidatingTextField(value = width,
                        resId = R.string.width_in_pixel,
                        message = if (viewModel.isWidthError()) stringResource(id = R.string.range_hint) else "",
                        imeAction = ImeAction.Next,
                        onValueChange = { viewModel.setWidth(it) })
                    ValidatingTextField(value = height,
                        resId = R.string.height_in_pixel,
                        imeAction = ImeAction.Next,
                        message = if (viewModel.isHeightError()) stringResource(id = R.string.range_hint) else "",
                        onValueChange = { viewModel.setHeight(it) })
                }
                if (LocalWindowSizeClass.current.widthSizeClass != WindowWidthSizeClass.Compact) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        widthAndHeight()
                    }
                } else {
                    widthAndHeight()
                }
                ValidatingTextField(
                    value = code,
                    onValueChange = { viewModel.setCode(it) },
                    resId = R.string.code,
                    message = if (viewModel.isCodeError()) stringResource(
                        when (state.formatIndex) {
                            1 -> R.string.must_be_12_or_13_digits
                            else -> R.string.cannot_be_empty
                        }
                    )
                    else "",
                    keyboardType = KeyboardType.Ascii,
                    imeAction = if (!viewModel.canGenerate()) ImeAction.Next else ImeAction.Done,
                )
                SingleChoiceSegmentedButtonRow(modifier = Modifier.align(alignment = Alignment.CenterHorizontally)) {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index, count = options.size
                            ), onClick = {
                                viewModel.setFormatIndex(index)
                            }, selected = index == state.formatIndex
                        ) {
                            Text(label)
                        }
                    }
                }
            }
            if (viewModel.canGenerate()) {
                FloatingActionButton(
                    onClick = {
                        viewModel.generate()
                    },
                    modifier = Modifier
                        .align(alignment = Alignment.BottomEnd)
                        .padding(all = 16.dp)
                        .navigationBarsPadding()
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(id = R.string.generate)
                    )
                }
            }
        }
        if (state.generatorExceptionMessage.isNotEmpty()) {
            AlertDialog(onDismissRequest = callback,
                confirmButton = {
                    Button(onClick = callback) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                },
                title = { Text(text = stringResource(id = R.string.could_not_generate_code)) },
                text = { Text(text = state.generatorExceptionMessage) })
        }
    }
}

@Composable
fun ValidatingTextField(
    value: String,
    @StringRes resId: Int,
    onValueChange: (String) -> Unit,
    message: String = "",
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Default,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        label = {
            Text(text = stringResource(id = resId))
        },
        isError = message.isNotEmpty(),
        supportingText = { Text(text = message) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
    )
}
