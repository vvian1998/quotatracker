package com.quotatracker.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================
// Design tokens v2 — "voucher / signal" redesign
// Grounded in Indonesian prepaid data vernacular: scratch
// vouchers, SIM signal bars, mobile-vs-WiFi quota accounting.
// One bold accent (Lime), Ember reserved strictly for warnings.
// ============================================================

// Surfaces — ink graphite, not navy
val Ink = Color(0xFF0A0C10)
val Surface = Color(0xFF14171F)
val SurfaceElevated = Color(0xFF1C202A)
val Hairline = Color(0xFF262B36)

// Accents
val Lime = Color(0xFFC8FF3D)
val LimeDim = Color(0xFF5C7A22)
val Ember = Color(0xFFFF6B4A)
val NeutralBucket = Color(0xFF31384A)

// Text
val TextHi = Color(0xFFF5F6F2)
val TextMid = Color(0xFF868C9C)
val TextLow = Color(0xFF4B505E)

// ------------------------------------------------------------
// Legacy aliases — kept so every existing screen (Settings,
// History, Detail, Onboarding, BottomNavBar…) inherits the new
// palette automatically through MaterialTheme, without a
// file-by-file rewrite. New code should prefer the tokens above;
// these are the old names, repointed to v2 values.
// ------------------------------------------------------------
val BackgroundDark = Ink
val SurfaceDark = Surface
val CardBackground = Surface
val CardBackgroundElevated = SurfaceElevated
val CardBorder = Hairline.copy(alpha = 0.8f)

val TealPrimary = Lime
val TealVariant = Lime
val TealGlow = Lime.copy(alpha = 0.18f)

val AmberAccent = Ember
val AmberGlow = Ember.copy(alpha = 0.18f)

val BlueWifi = Color(0xFF7C8AA6)
val BlueWifiGlow = BlueWifi.copy(alpha = 0.15f)

val RedWarning = Ember
val RedWarningGlow = Ember.copy(alpha = 0.18f)

val TextPrimary = TextHi
val TextSecondary = TextMid
val TextMuted = TextLow

// Gradients kept only for compile compatibility with older call
// sites; the v2 design intentionally avoids gradients/glow.
val TealToAmberGradient = Brush.horizontalGradient(colors = listOf(Lime, Ember))
val CardGlowGradient = Brush.verticalGradient(colors = listOf(SurfaceElevated, Surface))

val BubbleGlassBackground = Color(0xD914171F)
val BubbleBorder = Color(0x80C8FF3D)
