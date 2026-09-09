package com.islamic.bangla.data.repository

import com.islamic.bangla.data.local.dao.BookmarkDao
import com.islamic.bangla.data.model.Bookmark
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao
) {
    fun getAll(): Flow<List<Bookmark>> = bookmarkDao.getAll()

    fun getByType(type: String): Flow<List<Bookmark>> = bookmarkDao.getByType(type)

    suspend fun isBookmarked(type: String, refId: Int): Boolean =
        bookmarkDao.count(type, refId) > 0

    suspend fun add(bookmark: Bookmark) = bookmarkDao.insert(bookmark)

    suspend fun remove(type: String, refId: Int) = bookmarkDao.delete(type, refId)

    suspend fun toggle(type: String, refId: Int, title: String, subtitle: String) {
        if (isBookmarked(type, refId)) {
            remove(type, refId)
        } else {
            add(Bookmark(type = type, refId = refId, title = title, subtitle = subtitle))
        }
    }
}
