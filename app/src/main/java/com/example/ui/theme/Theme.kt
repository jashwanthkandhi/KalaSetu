package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val KalaLightColorScheme = lightColorScheme(
    primary = KalaPrimary,
    onPrimary = Color.White,
    primaryContainer = KalaBanner,
    onPrimaryContainer = KalaPrimaryDark,
    secondary = KalaSecondary,
    onSecondary = Color.White,
    secondaryContainer = KalaSecondaryContainer,
    onSecondaryContainer = KalaText,
    background = KalaBackground,
    onBackground = KalaText,
    surface = KalaSurface,
    onSurface = KalaText,
    surfaceVariant = KalaSurfaceVariant,
    onSurfaceVariant = KalaTextMuted,
    outline = KalaBorder,
    outlineVariant = KalaBorder,
    error = KalaError,
    onError = Color.White
)

private val KalaDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9E66),
    onPrimary = Color(0xFF4A1A05),
    primaryContainer = KalaPrimaryDark,
    onPrimaryContainer = Color(0xFFFFDCC5),
    secondary = KalaSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF422818),
    onSecondaryContainer = Color(0xFFFFE082),
    surfaceTint = Color(0xFFFF9E66),
    background = KalaBackgroundDark,
    onBackground = Color(0xFFF7F2EE),
    surface = KalaSurfaceDark,
    onSurface = Color(0xFFF7F2EE),
    surfaceVariant = KalaSurfaceVariantDark,
    onSurfaceVariant = Color(0xFFD6C8C0),
    outline = KalaBorderDark,
    outlineVariant = KalaBorderDark,
    error = Color(0xFFFF8A80),
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
