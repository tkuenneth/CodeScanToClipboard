package eu.thomaskuenneth.codescantoclipboard.screen

import android.view.View
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import eu.thomaskuenneth.codescantoclipboard.CodeScanToClipboardViewModel
import eu.thomaskuenneth.codescantoclipboard.IconButtonWithTooltip
import eu.thomaskuenneth.codescantoclipboard.R
import eu.thomaskuenneth.codescantoclipboard.defaultColorScheme

sealed class CodeScanToClipboardScreen(
    val route: String,
    @StringRes val label: Int,
    val icon: ImageVector
) {
    companion object {
        val screens = listOf(
            Scanner,
        )
    }

    object Scanner : CodeScanToClipboardScreen(
        "scanner",
        R.string.scanner,
        Icons.Default.Camera
    )
}


@Composable
fun CodeScanToClipboardScreen(
    viewModel: CodeScanToClipboardViewModel,
    root: View,
    shareCallback: (String) -> Unit
) {
    MaterialTheme(
        colorScheme = defaultColorScheme()
    ) {
        val navController = rememberNavController()
        Scaffold(
            topBar = {
                CodeScanToClipboardTopAppBar(
                    viewModel = viewModel,
                    shareCallback = shareCallback
                )
            },
            bottomBar = {
                CodeScanToClipboardBottomBar(
                    navController = navController
                )
            }
        ) {
            CodeScanToClipboardNavHost(
                navController = navController,
                viewModel = viewModel,
                root = root,
                paddingValues = it
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeScanToClipboardTopAppBar(
    viewModel: CodeScanToClipboardViewModel,
    shareCallback: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    with(state) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
    }
}

@Composable
fun CodeScanToClipboardBottomBar(navController: NavHostController) {
    BottomAppBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        CodeScanToClipboardScreen.screens.forEach { screen ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
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
    root: View,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = CodeScanToClipboardScreen.Scanner.route,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues = paddingValues)
    ) {
        composable(CodeScanToClipboardScreen.Scanner.route) {
            ScannerScreen(
                root = root,
                viewModel = viewModel,
            )
        }
    }
}
