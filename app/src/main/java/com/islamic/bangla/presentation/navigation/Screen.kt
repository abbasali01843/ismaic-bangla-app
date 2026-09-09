package com.islamic.bangla.presentation.navigation

/** Navigation destinations used in [AppNavGraph]. */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object QuranList : Screen("quran")
    data object SurahDetail : Screen("surah/{surahNumber}?startAyah={startAyah}") {
        fun createRoute(surahNumber: Int, startAyah: Int = 0) =
            "surah/$surahNumber?startAyah=$startAyah"
    }
    data object HadithList : Screen("hadith")
    data object DuaList : Screen("dua")
    data object PrayerTimes : Screen("prayer")
    data object Tasbih : Screen("tasbih")
    data object Qibla : Screen("qibla")
    data object Bookmarks : Screen("bookmarks")
    data object Search : Screen("search")
    data object AllahNames : Screen("allah_names")
    data object Kalima : Screen("kalima")
    data object NamazGuide : Screen("namaz_guide")
    data object Zakat : Screen("zakat")
    data object Settings : Screen("settings")
}
