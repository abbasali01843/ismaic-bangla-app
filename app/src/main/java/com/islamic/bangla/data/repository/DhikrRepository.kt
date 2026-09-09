package com.islamic.bangla.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.islamic.bangla.data.model.Dhikr
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Loads the bundled dhikr list from assets (tiny file, cached in memory). */
@Singleton
class DhikrRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var cached: List<Dhikr>? = null

    fun getDhikrs(): List<Dhikr> {
        cached?.let { return it }
        return try {
            val json = context.assets.open("dhikrs.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Dhikr>>() {}.type
            Gson().fromJson<List<Dhikr>>(json, type).also { cached = it }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
