package com.caderninho.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = InkBlue,
    secondary = SuccessGreen,
    tertiary = WarningAmber,
    error = ErrorRed,
    background = PaperLight,
    surface = CardPaperLight,
    surfaceVariant = ColorTokens.LightSelection,
    outline = InkBlue.copy(alpha = 0.55f)
)

private val DarkColors = darkColorScheme(
    primary = InkBlueLight,
    secondary = ColorTokens.DarkSuccess,
    tertiary = ColorTokens.DarkWarning,
    error = ColorTokens.DarkError,
    background = PaperDark,
    surface = CardPaperDark,
    surfaceVariant = ColorTokens.DarkSelection,
    outline = InkBlueLight.copy(alpha = 0.65f)
)

@Composable
fun LedgerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = CaderninhoTypography,
        content = content
    )
}

private object ColorTokens {
    val LightSelection = androidx.compose.ui.graphics.Color(0xFFDCEEF8)
    val DarkSelection = androidx.compose.ui.graphics.Color(0xFF294253)
    val DarkSuccess = androidx.compose.ui.graphics.Color(0xFF8CC99A)
    val DarkWarning = androidx.compose.ui.graphics.Color(0xFFFFB95C)
    val DarkError = androidx.compose.ui.graphics.Color(0xFFFFB4AB)
}
