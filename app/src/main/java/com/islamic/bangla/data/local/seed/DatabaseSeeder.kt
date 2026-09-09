package com.islamic.bangla.data.local.seed

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.islamic.bangla.data.model.Dua
import com.islamic.bangla.data.model.Quran
import com.islamic.bangla.data.repository.DuaRepository
import com.islamic.bangla.data.repository.QuranRepository
import kotlinx.coroutines.flow.first

/**
 * Seeds the Room database from JSON files bundled in `assets/` on first launch.
 * Seeding is skipped when tables already contain data.
 */
object DatabaseSeeder {

    private const val TAG = "DatabaseSeeder"

    suspend fun seedIfEmpty(
        context: Context,
        quranRepository: QuranRepository,
        duaRepository: DuaRepository
    ) {
        try {
            if (quranRepository.getAllSurahs().first().isEmpty()) {
                val surahs = loadSurahs(context)
                quranRepository.insertAllQuran(surahs)
                Log.i(TAG, "Seeded ${surahs.size} surahs")
            }
            if (duaRepository.getAllDuas().first().isEmpty()) {
                val duas = loadDuas(context)
                duaRepository.insertAllDuas(duas)
                Log.i(TAG, "Seeded ${duas.size} duas")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database seeding failed", e)
        }
    }

    private fun loadSurahs(context: Context): List<Quran> {
        val json = context.assets.open("surahs.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Quran>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun loadDuas(context: Context): List<Dua> {
        val json = context.assets.open("duas.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Dua>>() {}.type
        return Gson().fromJson(json, type)
    }
}
