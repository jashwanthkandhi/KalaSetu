package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// KalaSetu Official Color Tokens (SIH26090 Specification - Modern Luxury Artisan Theme)
val KalaBackground = Color(0xFFFBF8F5)     // Crisp warm ivory porcelain
val KalaBackgroundDark = Color(0xFF16110E) // Deep warm obsidian
val KalaSurface = Color(0xFFFFFFFF)        // Pure white — cards, inputs, bottom bar
val KalaSurfaceDark = Color(0xFF221A15)    // Elevated warm dark surface
val KalaSurfaceVariant = Color(0xFFF4EDE6) // Soft warm tint for secondary areas
val KalaSurfaceVariantDark = Color(0xFF2C221C)
val KalaPrimary = Color(0xFFC85A24)        // Vibrant handcrafted terracotta
val KalaPrimaryLight = Color(0xFFE2743B)   // Terracotta gradient highlight
val KalaPrimaryDark = Color(0xFF9C3C0F)    // Deep terracotta for pressed / dark states
val KalaSecondary = Color(0xFFD97706)      // Warm Saffron/Gold accent
val KalaSecondaryContainer = Color(0xFFFEF3C7) // Saffron-tinted chip/pill container
val KalaText = Color(0xFF221610)           // Deep espresso charcoal — high legibility
val KalaTextMuted = Color(0xFF7D6D64)      // Warm slate gray — subtitles, hints
val KalaSuccess = Color(0xFF16A34A)        // Emerald green — published status
val KalaSuccessContainer = Color(0xFFDCFCE7) // Soft green container
val KalaWarning = Color(0xFFD97706)        // Amber — draft status, offline sync
val KalaWarningContainer = Color(0xFFFEF3C7)
val KalaError = Color(0xFFDC2626)          // Crimson red — error messages
val KalaErrorContainer = Color(0xFFFEE2E2)
val KalaBanner = Color(0xFFFFF7ED)         // Soft warm peach-amber tint
val KalaBorder = Color(0xFFEBE2DA)         // Subtle warm divider/border stroke
val KalaBorderDark = Color(0xFF3E3129)
val KalaOverlay = Color(0x66221610)        // Semi-transparent deep brown overlay

// Backward-compatible placeholders for existing templates/tests
val Purple80 = KalaPrimary
val PurpleGrey80 = KalaSecondary
val Pink80 = KalaBanner
val Purple40 = KalaPrimary
val PurpleGrey40 = KalaTextMuted
val Pink40 = KalaWarning
