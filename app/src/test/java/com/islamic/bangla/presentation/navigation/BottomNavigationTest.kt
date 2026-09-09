package com.islamic.bangla.presentation.navigation

import androidx.navigation.createGraph
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.testing.TestNavigator
import androidx.navigation.testing.test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Regression tests for the bottom-navigation action
 * ([navigateToBottomDestination]) on a graph mirroring the production
 * [Screen] routes: Home is the start destination, the other four tabs and a
 * secondary screen hang off the same flat graph.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BottomNavigationTest {

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(RuntimeEnvironment.getApplication())
        // TestNavHostController normally registers this itself; tolerate both.
        try {
            navController.navigatorProvider.getNavigator<TestNavigator>("test")
        } catch (e: Exception) {
            navController.navigatorProvider.addNavigator(TestNavigator())
        }
        navController.graph = navController.createGraph(startDestination = "home") {
            test("home")
            test("quran")
            test("hadith")
            test("dua")
            test("prayer")
            test("surah/{surahNumber}")
            test("search")
        }
    }

    private fun routes(): List<String?> =
        navController.currentBackStack.value.map { it.destination.route }

    @Test
    fun homeTapFromAnotherTab_returnsHome() {
        navController.navigateToBottomDestination("quran")
        assertEquals("quran", navController.currentDestination?.route)

        navController.navigateToBottomDestination("home")

        assertEquals("home", navController.currentDestination?.route)
        assertEquals(listOf("home"), routes())
    }

    @Test
    fun everyBottomTab_isReachableWithoutDuplicates() {
        listOf("quran", "hadith", "dua", "prayer", "home").forEach { tab ->
            navController.navigateToBottomDestination(tab)
            assertEquals(tab, navController.currentDestination?.route)
        }
        assertEquals(listOf("home"), routes())
    }

    @Test
    fun tabSwitching_keepsSingleTabAboveHome() {
        navController.navigateToBottomDestination("quran")
        navController.navigateToBottomDestination("hadith")

        assertEquals("hadith", navController.currentDestination?.route)
        assertEquals(listOf("home", "hadith"), routes())
    }

    @Test
    fun retappingSelectedTab_pushesNothing() {
        navController.navigateToBottomDestination("quran")
        navController.navigateToBottomDestination("quran")
        navController.navigateToBottomDestination("quran")

        assertEquals(listOf("home", "quran"), routes())
    }

    @Test
    fun retappingHome_pushesNothing() {
        navController.navigateToBottomDestination("home")

        assertEquals("home", navController.currentDestination?.route)
        assertEquals(listOf("home"), routes())
    }

    @Test
    fun homeTapFromSecondaryScreen_popsEverythingAboveHome() {
        // Secondary screens are pushed with a plain navigate (like the Home
        // cards and the audio bar do in production).
        navController.navigate("surah/5")
        assertEquals(listOf("home", "surah/{surahNumber}"), routes())

        navController.navigateToBottomDestination("home")

        assertEquals("home", navController.currentDestination?.route)
        assertEquals(listOf("home"), routes())
    }

    @Test
    fun tabTapFromSecondaryScreen_resetsToThatTab() {
        navController.navigateToBottomDestination("quran")
        navController.navigate("surah/5")

        navController.navigateToBottomDestination("hadith")

        assertEquals("hadith", navController.currentDestination?.route)
        assertEquals(listOf("home", "hadith"), routes())
    }

    @Test
    fun backFromSecondaryScreen_returnsToPreviousTab() {
        navController.navigateToBottomDestination("quran")
        navController.navigate("surah/5")

        assertTrue(navController.popBackStack())

        assertEquals("quran", navController.currentDestination?.route)
        assertEquals(listOf("home", "quran"), routes())
    }

    @Test
    fun backFromTab_returnsHome() {
        navController.navigateToBottomDestination("dua")

        assertTrue(navController.popBackStack())

        assertEquals("home", navController.currentDestination?.route)
        assertEquals(listOf("home"), routes())
    }
}
