package com.islamic.bangla.presentation.theme

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Design system — light", showBackground = true)
@Composable
private fun DesignSystemPreviewLight() {
    IslamicBanglaTheme(darkTheme = false) {
        DesignSystemSheet()
    }
}

@Preview(
    name = "Design system — dark",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DesignSystemPreviewDark() {
    IslamicBanglaTheme(darkTheme = true) {
        DesignSystemSheet()
    }
}

/** Visual inventory of the design tokens: palette, type, shapes, dividers. */
@Composable
private fun DesignSystemSheet() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(
                horizontal = AppDimens.screenHorizontal,
                vertical = AppDimens.screenVertical
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
        ) {
            Text("স্ক্রিন শিরোনাম", style = MaterialTheme.typography.headlineSmall)
            Text("সেকশন শিরোনাম", style = MaterialTheme.typography.titleMedium)
            Text(
                "এটি একটি দীর্ঘ বাংলা অনুচ্ছেদ — ফজরের নামাজের পর কুরআন তিলাওয়াত " +
                    "এবং সকালের দোয়াগুলো পড়া দিনের সেরা শুরু।",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                "গৌণ তথ্য: পরবর্তী নামাজ যোহর — দুপুর ১২:০৫",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "ক্যাপশন • ১২৩৪৫৬৭৮৯০ • 1234567890",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = ArabicTextStyle,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
                style = ArabicHadithStyle,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "সূরা বাকারা • আয়াত ২০১",
                style = ArabicReferenceStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("২৭", style = NumbersStyle)
            HorizontalDivider(
                thickness = AppDimens.dividerThickness,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            PaletteRow()
            ShapesRow()
        }
    }
}

@Composable
private fun PaletteRow() {
    val scheme = MaterialTheme.colorScheme
    val swatches = listOf(
        "P" to scheme.primary,
        "PC" to scheme.primaryContainer,
        "S" to scheme.secondary,
        "SC" to scheme.secondaryContainer,
        "T" to scheme.tertiary,
        "TC" to scheme.tertiaryContainer,
        "E" to scheme.error,
        "EC" to scheme.errorContainer
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        swatches.forEach { (label, color) ->
            PaletteDot(label = label, color = color)
        }
    }
}

@Composable
private fun PaletteDot(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun ShapesRow() {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(AppShapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer)
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(AppShapes.medium)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(AppShapes.large)
                .background(MaterialTheme.colorScheme.tertiaryContainer)
        )
    }
}
