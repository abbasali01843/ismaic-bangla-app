package com.islamic.bangla.data

import com.islamic.bangla.data.local.dao.HadithDao
import com.islamic.bangla.data.model.Hadith
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** In-memory [HadithDao] for JVM/Robolectric tests (no Room needed). */
class FakeHadithDao : HadithDao {
    private val store = MutableStateFlow<List<Hadith>>(emptyList())

    override suspend fun insertHadith(hadith: Hadith) {
        store.update { current ->
            (current.filterNot { it.hadithId == hadith.hadithId } + hadith)
        }
    }

    override suspend fun insertAllHadiths(hadithList: List<Hadith>) {
        val ids = hadithList.map { it.hadithId }.toSet()
        store.update { current ->
            current.filterNot { it.hadithId in ids } + hadithList
        }
    }

    override fun getAllHadiths(): Flow<List<Hadith>> = store

    override fun getHadithsByCollection(collection: String): Flow<List<Hadith>> =
        store.map { list -> list.filter { it.collection == collection } }

    override fun getHadithById(hadithId: Int): Flow<Hadith> =
        store.map { list ->
            list.firstOrNull { it.hadithId == hadithId }
                ?: throw NoSuchElementException("No hadith $hadithId")
        }

    override fun searchHadiths(searchText: String): Flow<List<Hadith>> =
        store.map { list ->
            list.filter {
                it.banglaTranslation.contains(searchText, ignoreCase = true) ||
                    it.arabicText.contains(searchText, ignoreCase = true)
            }
        }

    override fun getHadithsByGrade(grade: String): Flow<List<Hadith>> =
        store.map { list -> list.filter { it.gradeOfAuthenticity == grade } }

    override fun getHadithCollections(): Flow<List<String>> =
        store.map { list -> list.map { it.collection }.distinct() }

    override suspend fun deleteHadithsByCollection(collection: String) {
        store.update { current -> current.filterNot { it.collection == collection } }
    }

    override suspend fun deleteAllHadiths() {
        store.value = emptyList()
    }
}
