package com.quotatracker.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Type roles, v2 ("voucher/signal" redesign).
 *
 * Two roles instead of one flat system default:
 *  - MeterFamily (monospace): numeric data readouts ONLY (display*,
 *    and labelMedium for small inline figures) — reads like a
 *    meter/counter, not decoration.
 *  - UiFamily (sans serif): everything else — labels, titles, body.
 *
 * This uses the platform's generic Monospace/SansSerif families so the
 * app builds with zero extra font dependencies or network fetches. If
 * you want the exact mockup fonts (JetBrains Mono + Plus Jakarta Sans),
 * drop the .ttf files into res/font/ and swap FontFamily.Monospace /
 * FontFamily.SansSerif below for FontFamily(Font(R.font.jetbrains_mono))
 * / FontFamily(Font(R.font.plus_jakarta_sans)) — everything else stays
 * the same since every screen reads fonts through these two roles.
 */
private val MeterFamily = FontFamily.Monospace
private val UiFamily = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MeterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 46.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.5).sp,
        color = TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = MeterFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        color = TextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        color = TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 27.sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.5.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.1.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.2.sp,
        color = TextSecondary
    ),
    labelMedium = TextStyle(
        fontFamily = MeterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        color = TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = UiFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.3.sp,
        color = TextMuted
    )
)
