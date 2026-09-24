package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// KalaSetu Official Color Tokens (SIH26090 Specification)
val KalaBackground = Color(0xFFF5F0EB)     // Warm cream — all screen backgrounds
val KalaSurface = Color(0xFFFFFFFF)        // White — cards, inputs, bottom bar
val KalaPrimary = Color(0xFFC4622D)        // Terracotta — primary buttons, mic, active states
val KalaPrimaryDark = Color(0xFFA24A1D)    // Deep terracotta for pressed / dark states
val KalaSecondary = Color(0xFFD4A017)      // Saffron/gold — AI price, badges, accents
val KalaText = Color(0xFF2C1810)           // Deep brown/charcoal — titles and primary body
val KalaTextMuted = Color(0xFF8A7A70)      // Warm gray — subtitles, hints, timestamps
val KalaSuccess = Color(0xFF4CAF50)        // Soft green — Saved status, checkmarks
val KalaWarning = Color(0xFFF59E0B)        // Amber — Draft chip, offline queue badge
val KalaError = Color(0xFFD32F2F)          // Muted red — error messages
val KalaBanner = Color(0xFFFFF3E0)         // Saffron-tint — AI review banner
val KalaBorder = Color(0xFFE8DDD5)         // Warm light brown — dividers, chip outlines
val KalaOverlay = Color(0x662C1810)        // Semi-transparent deep brown overlay

// Backward-compatible placeholders for existing templates/tests
val Purple80 = KalaPrimary
val PurpleGrey80 = KalaSecondary
val Pink80 = KalaBanner
val Purple40 = KalaPrimary
val PurpleGrey40 = KalaTextMuted
val Pink40 = KalaWarning
