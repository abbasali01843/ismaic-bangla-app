package com.islamic.bangla.presentation.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale. Prefer these over raw `.dp` values so spacing stays
 * rhythmic across screens. (Matches the values screens already converge on.)
 */
object AppSpacing {
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
}

/**
 * Layout dimensions: screen padding, component heights, radii (mirrors
 * [AppShapes]), dividers. Single place to tune the app's density.
 */
object AppDimens {
    /** Standard horizontal screen padding. */
    val screenHorizontal: Dp = 16.dp

    /** Standard vertical screen padding (top/bottom of content). */
    val screenVertical: Dp = 12.dp

    /** Space between content sections on a screen. */
    val sectionSpacing: Dp = 24.dp

    /** Space between items inside a section/list. */
    val itemSpacing: Dp = 8.dp

    /** Default inner padding of cards. */
    val cardPadding: Dp = 16.dp

    /** Minimum touch target (accessibility): buttons, icon buttons, chips. */
    val touchMin: Dp = 48.dp

    /** Standard filled/outlined button height. */
    val buttonHeight: Dp = 48.dp

    /** Large button height (primary calls to action). */
    val buttonHeightLarge: Dp = 52.dp

    /** Search field height. */
    val searchHeight: Dp = 56.dp

    /** Bottom navigation bar height (with labels). */
    val bottomNavHeight: Dp = 80.dp

    /** Small top app bar height. */
    val appBarHeight: Dp = 64.dp

    /** Standard icon size; decorative/illustrative icons may be larger. */
    val iconSize: Dp = 24.dp

    // Corner radii (must mirror AppShapes).
    val radiusCard: Dp = 16.dp
    val radiusDialog: Dp = 24.dp
    val radiusSheet: Dp = 28.dp

    /** Divider/border treatment: 1dp line in outlineVariant. */
    val dividerThickness: Dp = 1.dp
}

/**
 * Elevation system (Material 3 tonal levels). Prefer tonal surfaces over
 * shadows; use elevation only to lift floating content (dialogs, sheets,
 * player bar).
 */
object AppElevation {
    val level0: Dp = 0.dp
    val level1: Dp = 1.dp
    val level2: Dp = 3.dp
    val level3: Dp = 6.dp
    val level4: Dp = 8.dp
}
