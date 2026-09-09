package com.islamic.bangla.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner-radius system. Usage conventions:
 * - extraSmall (4dp): text fields, chips, small badges
 * - small (8dp): small cards, image thumbnails
 * - medium (16dp): standard cards (AppCard, list cards)
 * - large (24dp): dialogs, large cards, bottom sheets (top corners)
 * - extraLarge (28dp): hero cards, modal sheets
 */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
