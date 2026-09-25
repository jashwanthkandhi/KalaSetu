package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KalaLightColorScheme = lightColorScheme(
    primary = KalaPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = KalaBanner,
    onPrimaryContainer = KalaText,
    secondary = KalaSecondary,
    onSecondary = Color.White,
    secondaryContainer = KalaBanner,
    onSecondaryContainer = KalaText,
    background = KalaBackground,
    onBackground = KalaText,
    surface = KalaSurface,
    onSurface = KalaText,
    surfaceVariant = KalaBackground,
    onSurfaceVariant = KalaTextMuted,
    outline = KalaBorder,
    outlineVariant = KalaBorder,
    error = KalaError,
    onError = Color.White
)

private val KalaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB68D),
    onPrimary = Color(0xFF3B1705),
    primaryContainer = KalaPrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = KalaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF51321F),
    onSecondaryContainer = Color(0xFFFFDCC5),
    surfaceTint = Color(0xFFFFB68D),
    background = Color(0xFF1E1511),
    onBackground = Color(0xFFF5F0EB),
    surface = Color(0xFF281E19),
    onSurface = Color(0xFFF5F0EB),
    surfaceVariant = Color(0xFF352923),
    onSurfaceVariant = Color(0xFFC7B9B0),
    outline = Color(0xFF4D3D35),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional KalaSetu brand palette
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KalaDarkColorScheme else KalaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun KalaSetuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val base = if (darkTheme) KalaDarkColorScheme else KalaLightColorScheme
    val colors = if (highContrast) base.copy(
        onSurface = if (darkTheme) Color.White else Color(0xFF21120C),
        onSurfaceVariant = if (darkTheme) Color.White else Color(0xFF21120C),
        outline = if (darkTheme) Color.White else Color(0xFF21120C)
    ) else base
    MaterialTheme(colorScheme = colors, typography = Typography, content = content)
}
