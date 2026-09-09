package com.islamic.bangla.presentation.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaBearingTest {

    @Test
    fun dhakaBearing_matchesKnownValue() {
        // Dhaka -> Kaaba ≈ 277.5° (same value as the app's Dhaka fallback).
        assertEquals(277.5, QiblaViewModel.qiblaBearing(23.8103, 90.4125), 0.5)
    }

    @Test
    fun bearing_isNormalizedToCompassRange() {
        val bearing = QiblaViewModel.qiblaBearing(0.0, 0.0)
        assertTrue(bearing in 0.0..360.0)
        // Null Island -> Kaaba ≈ 58.5° north-east.
        assertEquals(58.5, bearing, 0.5)
    }
}
