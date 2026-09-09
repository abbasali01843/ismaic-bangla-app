package com.islamic.bangla.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: bundle Noto Sans Bengali + Amiri/Scheherazade New fonts in res/font
// for even better Bangla/Arabic typography (no code changes needed — only
// swap FontFamily.Default below).

/**
 * Centralized type scale. Line heights are generous on purpose: Bangla
 * conjuncts and Arabic diacritics need vertical air to stay readable.
 *
 * letterSpacing is 0 everywhere by design — non-zero letter spacing can
 * break Bangla conjunct shaping and Arabic ligatures.
 */
val AppTypography = Typography(
    // Screen title.
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Statistics / large numbers.
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),
    // Large section title.
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Section title.
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    // Small section title / card title.
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Primary body (Bangla translations, paragraphs).
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),
    // Secondary body.
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    // Buttons.
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    // Navigation labels.
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    // Captions (kept at 12sp: 11sp is too small for Bangla).
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )
)

/**
 * Large, airy style for Arabic verse text (Quran/Hadith/Dua). Line height is
 * ~2x so diacritics and tall glyphs never collide between lines. Call sites
 * align it right (RTL) — shaping itself is handled by the text engine.
 */
val ArabicTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 48.sp,
    letterSpacing = 0.sp
)

/** Comfortable Arabic size for hadith/dua body text. */
val ArabicHadithStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp
)

/** Small Arabic for references (surah names, narrators, book titles). */
val ArabicReferenceStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp
)

/** Statistics / counter numbers (tasbih, prayer counts). */
val NumbersStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.SemiBold,
    fontSize = 26.sp,
    lineHeight = 34.sp,
    letterSpacing = 0.sp
)

/** User-adjustable font scales (see Settings). Provided at the nav-graph root. */
data class FontScales(
    val arabic: Float = 1f,
    val bangla: Float = 1f
)

val LocalFontScales = compositionLocalOf { FontScales() }

@Composable
fun ScaledArabicTextStyle(): TextStyle {
    val scale = LocalFontScales.current.arabic
    return remember(scale) {
        ArabicTextStyle.copy(
            fontSize = ArabicTextStyle.fontSize * scale,
            lineHeight = ArabicTextStyle.lineHeight * scale
        )
    }
}

@Composable
fun ScaledBanglaTextStyle(): TextStyle {
    val base = MaterialTheme.typography.bodyLarge
    val scale = LocalFontScales.current.bangla
    return remember(base, scale) {
        base.copy(
            fontSize = base.fontSize * scale,
            lineHeight = base.lineHeight * scale
        )
    }
}
