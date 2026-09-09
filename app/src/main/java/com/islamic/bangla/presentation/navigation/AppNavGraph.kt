package com.islamic.bangla.presentation.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.islamic.bangla.presentation.components.AudioPlayerBar
import com.islamic.bangla.presentation.screen.AllahNamesScreen
import com.islamic.bangla.presentation.screen.BookmarksScreen
import com.islamic.bangla.presentation.screen.DuaListScreen
import com.islamic.bangla.presentation.screen.HadithListScreen
import com.islamic.bangla.presentation.screen.HomeScreen
import com.islamic.bangla.presentation.screen.KalimaScreen
import com.islamic.bangla.presentation.screen.NamazGuideScreen
import com.islamic.bangla.presentation.screen.PrayerTimeScreen
import com.islamic.bangla.presentation.screen.QiblaScreen
import com.islamic.bangla.presentation.screen.QuranListScreen
import com.islamic.bangla.presentation.screen.SearchScreen
import com.islamic.bangla.presentation.screen.SettingsScreen
import com.islamic.bangla.presentation.screen.SurahDetailScreen
import com.islamic.bangla.presentation.screen.TasbihScreen
import com.islamic.bangla.presentation.screen.ZakatScreen
import com.islamic.bangla.presentation.theme.FontScales
import com.islamic.bangla.presentation.theme.LocalFontScales
import com.islamic.bangla.presentation.viewmodel.PlayerViewModel
import com.islamic.bangla.presentation.viewmodel.SettingsViewModel

private data class BottomDestination(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

private val bottomDestinations = listOf(
    BottomDestination(Screen.Home, "হোম", Icons.Filled.Home),
    BottomDestination(Screen.QuranList, "কুরআন", Icons.Filled.MenuBook),
    BottomDestination(Screen.HadithList, "হাদিস", Icons.Filled.LibraryBooks),
    BottomDestination(Screen.DuaList, "দোয়া", Icons.Filled.Favorite),
    BottomDestination(Screen.PrayerTimes, "নামাজ", Icons.Filled.AccessTime)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val playlistSurah by playerViewModel.playlistSurah.collectAsStateWithLifecycle()
    val playlistSurahName by playerViewModel.playlistSurahName.collectAsStateWithLifecycle()
    val currentAyah by playerViewModel.currentAyah.collectAsStateWithLifecycle()
    val isPlaying by playerViewModel.isPlaying.collectAsStateWithLifecycle()
    val isLoading by playerViewModel.isLoading.collectAsStateWithLifecycle()
    val reciter by playerViewModel.reciter.collectAsStateWithLifecycle()
    val playerError by playerViewModel.error.collectAsStateWithLifecycle()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val arabicFontScale by settingsViewModel.arabicFontScale.collectAsStateWithLifecycle()
    val banglaFontScale by settingsViewModel.banglaFontScale.collectAsStateWithLifecycle()
    val bottomRoutes = bottomDestinations.map { it.screen.route }

    CompositionLocalProvider(
        LocalFontScales provides FontScales(arabicFontScale, banglaFontScale)
    ) {
        Scaffold(
            bottomBar = {
                Column {
                    if (playlistSurah > 0) {
                        AudioPlayerBar(
                            surahName = playlistSurahName,
                            currentAyah = currentAyah,
                            isPlaying = isPlaying,
                            isLoading = isLoading,
                            reciter = reciter,
                            reciters = playerViewModel.reciters,
                            error = playerError,
                            onToggle = { playerViewModel.toggle() },
                            onNext = { playerViewModel.next() },
                            onPrev = { playerViewModel.prev() },
                            onClose = { playerViewModel.stop() },
                            onReciterChange = { playerViewModel.setReciter(it) },
                            onBodyClick = {
                                navController.navigate(
                                    Screen.SurahDetail.createRoute(playlistSurah)
                                )
                            }
                        )
                    }
                    val backStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = backStackEntry?.destination?.route
                    if (currentRoute in bottomRoutes) {
                        NavigationBar {
                            bottomDestinations.forEach { destination ->
                                NavigationBarItem(
                                    selected = currentRoute == destination.screen.route,
                                    onClick = {
                                        navController.navigate(destination.screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.label
                                        )
                                    },
                                    label = { Text(destination.label) }
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onSearchClick = { navController.navigate(Screen.Search.route) },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.QuranList.route) {
                    QuranListScreen(
                        onSurahClick = { surahNumber ->
                            navController.navigate(Screen.SurahDetail.createRoute(surahNumber))
                        }
                    )
                }
                composable(
                    route = Screen.SurahDetail.route,
                    arguments = listOf(
                        navArgument("surahNumber") { type = NavType.IntType },
                        navArgument("startAyah") { type = NavType.IntType; defaultValue = 0 }
                    )
                ) { entry ->
                    SurahDetailScreen(
                        surahNumber = entry.arguments?.getInt("surahNumber") ?: 1,
                        startAyah = entry.arguments?.getInt("startAyah") ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.HadithList.route) {
                    HadithListScreen()
                }
                composable(Screen.DuaList.route) {
                    DuaListScreen()
                }
                composable(Screen.PrayerTimes.route) {
                    PrayerTimeScreen()
                }
                composable(Screen.Tasbih.route) {
                    TasbihScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Qibla.route) {
                    QiblaScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Bookmarks.route) {
                    BookmarksScreen(
                        onAyahClick = { surahNumber, ayahNumber ->
                            navController.navigate(
                                Screen.SurahDetail.createRoute(surahNumber, ayahNumber)
                            )
                        },
                        onHadithClick = { navController.navigate(Screen.HadithList.route) },
                        onDuaClick = { navController.navigate(Screen.DuaList.route) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Search.route) {
                    SearchScreen(
                        onAyahClick = { surahNumber, ayahNumber ->
                            navController.navigate(
                                Screen.SurahDetail.createRoute(surahNumber, ayahNumber)
                            )
                        },
                        onHadithClick = { navController.navigate(Screen.HadithList.route) },
                        onDuaClick = { navController.navigate(Screen.DuaList.route) },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.AllahNames.route) {
                    AllahNamesScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Kalima.route) {
                    KalimaScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.NamazGuide.route) {
                    NamazGuideScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Zakat.route) {
                    ZakatScreen(onBack = { navController.popBackStack() })
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
