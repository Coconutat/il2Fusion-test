package com.tools.il2fusion.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AmberNight,
    onPrimary = Night,
    secondary = NightInk,
    onSecondary = Night,
    tertiary = Moss,
    background = Night,
    onBackground = NightInk,
    surface = NightSurface,
    onSurface = NightInk,
    surfaceVariant = NightSurfaceSoft,
    onSurfaceVariant = NightInkSoft,
    outline = NightInkSoft,
    outlineVariant = NightSurfaceSoft
)

private val LightColorScheme = lightColorScheme(
    primary = SteelBlue,
    onPrimary = Cream,
    secondary = Clay,
    onSecondary = Cream,
    tertiary = Moss,
    background = Paper,
    onBackground = Ink,
    surface = SurfaceWarm,
    onSurface = Ink,
    surfaceVariant = SurfaceWarmStrong,
    onSurfaceVariant = InkSoft,
    outline = OutlineWarm,
    outlineVariant = OutlineSoft
)

@Composable
fun Il2FusionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> DarkColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !darkTheme -> LightColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
