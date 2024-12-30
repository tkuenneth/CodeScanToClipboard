package eu.thomaskuenneth.codescantoclipboard.screen

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel
import eu.thomaskuenneth.codescantoclipboard.IconButtonWithTooltip
import eu.thomaskuenneth.codescantoclipboard.LocalWindowSizeClass
import eu.thomaskuenneth.codescantoclipboard.R
import eu.thomaskuenneth.codescantoclipboard.defaultColorScheme

sealed class CodeScanToClipboardScreen(
    val route: String, @StringRes val label: Int, val icon: ImageVector
) {
    companion object {
        val screens = listOf(
            Scanner, Creator
        )
    }

    data object Scanner : CodeScanToClipboardScreen(
        "scanner", R.string.scanner, Icons.Default.Camera
    )

    data object Creator : CodeScanToClipboardScreen(
        "creator", R.string.creator, Icons.Default.Create
    )
}

@Composable
fun CodeScanToClipboardScreen(
    viewModel: CodeScanToClipboardViewModel,
    root: DecoratedBarcodeView,
    shareCallback: (String) -> Unit,
    scanImageFileCallback: () -> Unit
) {
    MaterialTheme(
        colorScheme = defaultColorScheme()
    ) {
        val useNavigationRail =
            LocalWindowSizeClass.current.widthSizeClass > WindowWidthSizeClass.Compact
        val navController = rememberNavController()
        val state by viewModel.uiState.collectAsState()
        val callback = {
            viewModel.setShowScanImageFileError(showScanFromFileError = false)
        }
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            if (useNavigationRail) CodeScanToClipboardNavigationRail(
                navController = navController
            )
            Scaffold(topBar = {
                CodeScanToClipboardTopAppBar(
                    viewModel = viewModel,
                    shareCallback = shareCallback,
                    scanImageFileCallback = scanImageFileCallback
                )
            }, bottomBar = {
                if (!useNavigationRail) CodeScanToClipboardBottomBar(
                    navController = navController
                )
            }) {
                val paddingValues = object : PaddingValues {
                    override fun calculateTopPadding(): Dp = it.calculateTopPadding()
                    override fun calculateBottomPadding(): Dp =
                        if (useNavigationRail) 0.dp else it.calculateBottomPadding()

                    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
                        it.calculateLeftPadding(layoutDirection)

                    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
                        it.calculateRightPadding(layoutDirection)
                }
                CodeScanToClipboardNavHost(
                    navController = navController,
                    viewModel = viewModel,
                    root = root,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                )
            }
        }
        if (state.showScanImageFileError) {
            AlertDialog(onDismissRequest = { callback() },
                confirmButton = {
                    TextButton(onClick = { callback() }) { Text(stringResource(id = R.string.ok)) }
                },
                title = { Text(text = stringResource(id = R.string.scan_image_file)) },
                text = { Text(text = stringResource(id = R.string.no_supported_formats_found)) })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScanToClipboardTopAppBar(
    viewModel: CodeScanToClipboardViewModel,
    shareCallback: (String) -> Unit,
    scanImageFileCallback: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scannerUiState by viewModel.scannerUiState.collectAsState()
    val flashOn = scannerUiState.flashOn
    val lastScannedText = scannerUiState.lastScannedText
    CenterAlignedTopAppBar(title = {
        Text(
            text = stringResource(id = R.string.app_name),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }, actions = {
        if (state.showScannerActions) {
            IconButtonWithTooltip(
                onClick = { viewModel.toggleFlash() },
                painter = painterResource(id = if (flashOn) R.drawable.baseline_flash_on_24 else R.drawable.baseline_flash_off_24),
                contentDescription = stringResource(id = if (flashOn) R.string.flash_on else R.string.flash_off)
            )
            if (lastScannedText.isNotEmpty()) {
                IconButtonWithTooltip(
                    onClick = {
                        shareCallback(lastScannedText)
                        viewModel.clearLastScannedText()
                    },
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
        OptionsMenu(
            showScannerActions = state.showScannerActions, onClick = scanImageFileCallback
        )
    })
}

@Composable
fun CodeScanToClipboardBottomBar(navController: NavHostController) {
    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        CodeScanToClipboardScreen.screens.forEach { screen ->
            NavigationBarItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                label = {
                    Text(text = stringResource(id = screen.label))
                },
                icon = {
                    Icon(
                        imageVector = screen.icon,
                        contentDescription = stringResource(id = screen.label)
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun CodeScanToClipboardNavHost(
    navController: NavHostController,
    viewModel: CodeScanToClipboardViewModel,
    root: DecoratedBarcodeView,
    modifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = CodeScanToClipboardScreen.Scanner.route,
        modifier = modifier
    ) {
        composable(CodeScanToClipboardScreen.Scanner.route) {
            root.resume()
            ScannerScreen(
                root = root,
                viewModel = viewModel,
            )
        }
        composable(CodeScanToClipboardScreen.Creator.route) {
            root.pause()
            CreatorScreen(
                viewModel = viewModel,
            )
        }
    }
}

@Composable
fun CodeScanToClipboardNavigationRail(navController: NavHostController) {
    NavigationRail {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    start = WindowInsets.displayCutout
                        .asPaddingValues()
                        .calculateLeftPadding(
                            LocalLayoutDirection.current
                        )
                ), verticalArrangement = Arrangement.Center
        ) {
            CodeScanToClipboardScreen.screens.forEach { screen ->
                NavigationRailItem(selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    label = {
                        Text(text = stringResource(id = screen.label))
                    },
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = stringResource(id = screen.label)
                        )
                    },
                    alwaysShowLabel = true
                )
            }
        }
    }
}

@Composable
fun OptionsMenu(
    showScannerActions: Boolean, onClick: () -> Unit
) {
    var menuOpened by remember { mutableStateOf(false) }
    val list = mutableListOf<@Composable () -> Unit>()
    if (showScannerActions) list.add {
        DropdownMenuItem(onClick = {
            menuOpened = false
            onClick()
        }, text = {
            Text(stringResource(id = R.string.scan_image_file))
        })
    }
    if (list.isNotEmpty()) {
        Box {
            IconButton(onClick = {
                menuOpened = true
            }) {
                Icon(Icons.Default.MoreVert, stringResource(id = R.string.options_menu))
            }
            DropdownMenu(expanded = menuOpened, onDismissRequest = {
                menuOpened = false
            }) {
                list.forEach { it() }
            }
        }
    }
}
