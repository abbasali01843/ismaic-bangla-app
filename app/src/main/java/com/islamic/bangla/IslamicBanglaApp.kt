package com.islamic.bangla

import android.app.Application
import com.islamic.bangla.data.local.seed.DatabaseSeeder
import com.islamic.bangla.data.repository.DuaRepository
import com.islamic.bangla.data.repository.QuranRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class IslamicBanglaApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SeederEntryPoint {
        fun quranRepository(): QuranRepository
        fun duaRepository(): DuaRepository
    }

    override fun onCreate() {
        super.onCreate()
        seedDatabase()
    }

    private fun seedDatabase() {
        val entryPoint = EntryPointAccessors.fromApplication(this, SeederEntryPoint::class.java)
        applicationScope.launch {
            DatabaseSeeder.seedIfEmpty(
                this@IslamicBanglaApp,
                entryPoint.quranRepository(),
                entryPoint.duaRepository()
            )
        }
    }
}
