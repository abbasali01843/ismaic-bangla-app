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
// for better Bangla/Arabic typography.
val AppTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 22.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
)

/** Large, airy style for Arabic (Quranic) text. */
val ArabicTextStyle = TextStyle(
    fontFamily = FontFamily.Default,
    fontWeight = FontWeight.Normal,
    fontSize = 22.sp,
    lineHeight = 40.sp
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
