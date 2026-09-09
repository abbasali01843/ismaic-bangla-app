package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.DuaDao
import com.islamic.bangla.data.model.Dua
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DuaRepository @Inject constructor(
    private val duaDao: DuaDao
) {
    fun getAllDuas(): Flow<List<Dua>> = duaDao.getAllDuas()

    fun getDuaById(duaId: Int): Flow<Dua> = duaDao.getDuaById(duaId)

    fun searchDuas(searchText: String): Flow<List<Dua>> = duaDao.searchDuas(searchText)

    fun getDuasByCategory(category: String): Flow<List<Dua>> = duaDao.getDuasByCategory(category)

    fun getCategories(): Flow<List<String>> = duaDao.getCategories()

    suspend fun insertDua(dua: Dua) = duaDao.insertDua(dua)

    suspend fun insertAllDuas(duaList: List<Dua>) = duaDao.insertAllDuas(duaList)

    suspend fun deleteAllDuas() = duaDao.deleteAllDuas()
}
