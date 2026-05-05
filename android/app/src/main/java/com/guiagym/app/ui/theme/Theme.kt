package com.guiagym.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary          = Orange40,
    onPrimary        = Neutral99,
    primaryContainer = Orange80,
    secondary        = Navy40,
    onSecondary      = Neutral99,
    background       = Neutral99,
    onBackground     = Neutral10,
    surface          = Neutral99,
    onSurface        = Neutral10,
)

private val DarkColors = darkColorScheme(
    primary          = Orange80,
    onPrimary        = Neutral10,
    primaryContainer = Orange40,
    secondary        = Navy80,
    onSecondary      = Neutral10,
)

@Composable
fun GuiaGymTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else      -> LightColors
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
