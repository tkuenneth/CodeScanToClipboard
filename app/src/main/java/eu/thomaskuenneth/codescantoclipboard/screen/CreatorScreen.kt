package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroupMenuState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel
import eu.thomaskuenneth.codescantoclipboard.LocalWindowSizeClass
import eu.thomaskuenneth.codescantoclipboard.R

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun CreatorScreen(
    viewModel: CodeScanToClipboardViewModel,
) {
    viewModel.setShowScannerActions(showActions = false)
    val state by viewModel.generatorUiState.collectAsState()
    val options = listOf(
        stringResource(id = R.string.qrcode),
        stringResource(id = R.string.ean13),
        stringResource(id = R.string.aztec),
        stringResource(id = R.string.data_matrix),
    )
    val callback = { viewModel.setGeneratorExceptionMessage("") }
    val scrollState = rememberScrollState()
    var scrollPosition by remember { mutableIntStateOf(0) }
    with(state) {
        Box {
            LocalWindowSizeClass.current.heightSizeClass.let {
                LaunchedEffect(scrollPosition) {
                    if (it != WindowHeightSizeClass.Expanded) {
                        scrollState.animateScrollTo(scrollPosition)
                    }
                }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .padding(top = 16.dp)
            ) {
                val imeAction = if (viewModel.canGenerate()) ImeAction.Done else ImeAction.Next
                val widthAndHeight: @Composable () -> Unit = {
                    ValidatingTextField(
                        value = width,
                        resId = R.string.width_in_pixel,
                        message = if (viewModel.isWidthError()) stringResource(id = R.string.range_hint) else "",
                        imeAction = imeAction,
                        onValueChange = { viewModel.setWidth(it) }) { scrollPosition = it }
                    ValidatingTextField(
                        value = height,
                        resId = R.string.height_in_pixel,
                        imeAction = imeAction,
                        message = if (viewModel.isHeightError()) stringResource(id = R.string.range_hint) else "",
                        onValueChange = { viewModel.setHeight(it) }) { scrollPosition = it }
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                        imeAction = imeAction,
                    ) { scrollPosition = it }
                    val overflowMenuButton: @Composable (ButtonGroupMenuState) -> Unit = remember {
                        { menuState ->
                            FilledIconButton(
                                onClick = {
                                    if (menuState.isShowing) {
                                        menuState.dismiss()
                                    } else {
                                        menuState.show()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(id = R.string.options_menu)
                                )
                            }
                        }
                    }
                    ButtonGroup(
                        overflowIndicator = overflowMenuButton,
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        options.forEachIndexed { index, label ->
                            toggleableItem(
                                onCheckedChange = {
                                    viewModel.setFormatIndex(index)
                                },
                                checked = index == state.formatIndex,
                                label = label,
                                icon = null
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.imePadding())
            }
            if (viewModel.canGenerate()) {
                FloatingActionButton(
                    onClick = {
                        viewModel.generate()
                    },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
            AlertDialog(
                onDismissRequest = callback,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ValidatingTextField(
    value: String,
    @StringRes resId: Int,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    message: String = "",
    keyboardType: KeyboardType = KeyboardType.Number,
    imeAction: ImeAction = ImeAction.Default,
    requestScrollTo: (Int) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var y by remember { mutableIntStateOf(0) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .focusRequester(focusRequester)
            .onGloballyPositioned {
                y = it.positionInParent().y.toInt()
            }
            .onFocusChanged {
                if (it.hasFocus) {
                    requestScrollTo(y)
                }
            },
        singleLine = true,
        label = {
            Text(text = stringResource(id = resId))
        },
        isError = message.isNotEmpty(),
        supportingText = { Text(text = message) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = {
            focusManager.moveFocus(FocusDirection.Next)
        }, onDone = {
            focusManager.clearFocus()
        }),
        shape = MaterialTheme.shapes.medium
    )
}
