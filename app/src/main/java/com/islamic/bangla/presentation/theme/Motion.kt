package com.islamic.bangla.presentation.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween

/**
 * Motion guidelines (durations + easings). Keep motion subtle:
 * - fast: press feedback, selection, small fades
 * - medium: expand/collapse, sheet/dialog enter/exit, navigation fades
 * - slow: large layout transitions (rare)
 *
 * Motion must never block input or harm accessibility; respect the system
 * animator settings (Compose handles reduced-motion scaling).
 */
object AppMotion {
    const val fast: Int = 150
    const val medium: Int = 300
    const val slow: Int = 450

    /** Default easing for most UI motion. */
    val easingStandard: Easing = FastOutSlowInEasing

    /** Easing for elements entering the screen. */
    val easingEnter: Easing = LinearOutSlowInEasing

    /** Emphasized easing for important transitions (M3 emphasized curve). */
    val easingEmphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /** Standard fade/slide tween for navigation and selection changes. */
    fun <T> standardTween(durationMillis: Int = medium) =
        tween<T>(durationMillis = durationMillis, easing = easingStandard)
}
