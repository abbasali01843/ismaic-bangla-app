package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.LastReadDao
import com.islamic.bangla.data.model.LastRead
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LastReadRepository @Inject constructor(
    private val lastReadDao: LastReadDao
) {
    fun get(): Flow<LastRead?> = lastReadDao.get()

    suspend fun save(lastRead: LastRead) = lastReadDao.save(lastRead)

    suspend fun clear() = lastReadDao.clear()
}
