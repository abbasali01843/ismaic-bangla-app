package com.islamic.bangla.data.repository

import android.content.Context
import com.google.gson.Gson
import com.islamic.bangla.data.model.AllahName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/** Static content: 99 Names bundled in assets (always offline). */
@Singleton
class AllahNamesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private var cached: List<AllahName>? = null

    fun getNames(): Flow<List<AllahName>> = flow {
        cached?.let {
            emit(it)
            return@flow
        }
        val json = context.assets.open("allah_names.json")
            .bufferedReader()
            .use { it.readText() }
        val names = gson.fromJson(json, Array<AllahName>::class.java).toList()
        cached = names
        emit(names)
    }.flowOn(Dispatchers.IO)
}
