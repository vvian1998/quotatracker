package com.quotatracker.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Background & Surfaces (Navy / Charcoal palette)
val BackgroundDark = Color(0xFF0F1923)
val SurfaceDark = Color(0xFF16222F)
val CardBackground = Color(0xFF1A2633)
val CardBackgroundElevated = Color(0xFF223142)
val CardBorder = Color(0x334E657E)

// Accents
val TealPrimary = Color(0xFF00BFA5)
val TealVariant = Color(0xFF64FFDA)
val TealGlow = Color(0x4000BFA5)

val AmberAccent = Color(0xFFFFB300)
val AmberGlow = Color(0x40FFB300)

val BlueWifi = Color(0xFF29B6F6)
val BlueWifiGlow = Color(0x4029B6F6)

val RedWarning = Color(0xFFFF5252)
val RedWarningGlow = Color(0x40FF5252)

// Text Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF8FA0B3)
val TextMuted = Color(0xFF55697D)

// Gradients
val TealToAmberGradient = Brush.horizontalGradient(
    colors = listOf(TealPrimary, AmberAccent)
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(CardBackgroundElevated, CardBackground)
)

val BubbleGlassBackground = Color(0xD91A2633)
val BubbleBorder = Color(0x8000BFA5)
