package eu.thomaskuenneth.codescantoclipboard

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButtonWithTooltip(
    onClick: () -> Unit, painter: Painter, contentDescription: String
) {
    PlainTooltipBox(tooltip = {
        Text(text = contentDescription)
    }) {
        IconButton(
            onClick = onClick, modifier = Modifier.tooltipAnchor()
        ) {
            Icon(
                painter = painter, contentDescription = contentDescription
            )
        }
    }
}

// https://github.com/tkuenneth/compose_adaptive_scaffold/blob/main/adaptivescaffold/src/main/java/eu/thomaskuenneth/adaptivescaffold/DefaultColorScheme.kt

@Composable
fun defaultColorScheme() = with(isSystemInDarkTheme()) {
    val hasDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    when (this) {
        true -> if (hasDynamicColor) {
            dynamicDarkColorScheme(context)
        } else {
            darkColorScheme()
        }

        false -> if (hasDynamicColor) {
            dynamicLightColorScheme(context)
        } else {
            lightColorScheme()
        }
    }
}
