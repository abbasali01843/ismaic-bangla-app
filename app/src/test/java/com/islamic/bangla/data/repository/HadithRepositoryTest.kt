package com.islamic.bangla.data.repository

import com.google.gson.JsonIOException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSyntaxException
import com.islamic.bangla.data.FakeHadithApi
import com.islamic.bangla.data.FakeHadithDao
import com.islamic.bangla.data.remote.dto.HadithDto
import com.islamic.bangla.data.remote.dto.HadithEditionResponse
import com.islamic.bangla.data.remote.dto.HadithMetadataDto
import com.islamic.bangla.data.remote.dto.HadithReferenceDto
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

/**
 * Pure-JVM tests for [HadithRepository.refreshCollection]: success mapping,
 * Arabic best-effort, and the typed error taxonomy (no more blanket
 * "no internet" for every failure).
 */
class HadithRepositoryTest {

    private lateinit var dao: FakeHadithDao
    private lateinit var api: FakeHadithApi
    private lateinit var repository: HadithRepository

    @Before
    fun setup() {
        dao = FakeHadithDao()
        api = FakeHadithApi()
        repository = HadithRepository(dao, api)
    }

    private fun benHadith(number: Int, text: String, book: Int = 1) = HadithDto(
        hadithnumber = JsonPrimitive(number),
        text = text,
        reference = HadithReferenceDto(
            book = JsonPrimitive(book),
            hadith = JsonPrimitive(number)
        )
    )

    @Test
    fun success_cachesBanglaAndArabic() = runTest {
        api.handler = { edition ->
            when (edition) {
                "ben-bukhari" -> HadithEditionResponse(
                    metadata = HadithMetadataDto(),
                    hadiths = listOf(benHadith(1, "বাংলা ১"), benHadith(2, "বাংলা ২"))
                )
                "ara-bukhari" -> HadithEditionResponse(
                    metadata = HadithMetadataDto(),
                    hadiths = listOf(
                        HadithDto(hadithnumber = JsonPrimitive(1), text = "عربي ١"),
                        HadithDto(hadithnumber = JsonPrimitive(2), text = "عربي ٢")
                    )
                )
                else -> throw AssertionError("unexpected edition $edition")
            }
        }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Success)
        assertTrue((result as HadithRefreshResult.Success).arabicComplete)
        assertEquals(listOf("ben-bukhari", "ara-bukhari"), api.requestedEditions)
        val cached = dao.getHadithsByCollection("bukhari").first()
        assertEquals(2, cached.size)
        assertEquals("বাংলা ১", cached[0].banglaTranslation)
        assertEquals("عربي ١", cached[0].arabicText)
        assertEquals("1", cached[0].hadithNumber)
        assertEquals("সহীহ", cached[0].gradeOfAuthenticity)
    }

    @Test
    fun arabicFailure_stillCachesBanglaAsPartial() = runTest {
        api.handler = { edition ->
            if (edition == "ben-bukhari") {
                FakeHadithApi.editionOf(benHadith(7, "বাংলা ৭"))
            } else {
                throw UnknownHostException("cdn down for ara only")
            }
        }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Success)
        result as HadithRefreshResult.Success
        assertTrue(!result.arabicComplete)
        assertTrue(result.arabicError is HadithLoadError.NoInternet)
        val cached = dao.getHadithsByCollection("bukhari").first()
        assertEquals(1, cached.size)
        assertEquals("বাংলা ৭", cached[0].banglaTranslation)
        assertEquals("", cached[0].arabicText)
    }

    @Test
    fun banglaUnknownHost_returnsNoInternetAndKeepsCache() = runTest {
        dao.insertAllHadiths(
            listOf(
                com.islamic.bangla.data.model.Hadith(
                    hadithId = 1,
                    collection = "bukhari",
                    bookName = "",
                    hadithNumber = "1",
                    arabicText = "",
                    banglaTranslation = "cached",
                    englishTranslation = "",
                    gradeOfAuthenticity = ""
                )
            )
        )
        api.handler = { throw UnknownHostException("no route") }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Failure)
        assertTrue(
            (result as HadithRefreshResult.Failure).error is HadithLoadError.NoInternet
        )
        // Failed refresh must not wipe previously cached hadiths.
        assertEquals(1, dao.getHadithsByCollection("bukhari").first().size)
    }

    @Test
    fun http500_returnsServerErrorWithCode() = runTest {
        api.handler = {
            throw HttpException(Response.error<Any>(500, "boom".toResponseBody()))
        }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Failure)
        val error = (result as HadithRefreshResult.Failure).error
        assertTrue(error is HadithLoadError.ServerError)
        assertEquals(500, (error as HadithLoadError.ServerError).httpCode)
        assertTrue(dao.getAllHadiths().first().isEmpty())
    }

    @Test
    fun malformedPayload_returnsDataFormatError() = runTest {
        api.handler = { throw JsonSyntaxException("bad json") }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Failure)
        assertTrue(
            (result as HadithRefreshResult.Failure).error is HadithLoadError.DataFormatError
        )
    }

    @Test
    fun networkStallWrappedInParserException_stillNoInternet() = runTest {
        // Retrofit/Gson wrap a mid-download stall: the cause chain must win.
        api.handler = {
            throw JsonIOException(SocketTimeoutException("read timed out"))
        }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Failure)
        assertTrue(
            (result as HadithRefreshResult.Failure).error is HadithLoadError.NoInternet
        )
    }

    @Test
    fun emptyEdition_returnsEmptyCollection() = runTest {
        api.handler = { HadithEditionResponse() }

        val result = repository.refreshCollection("bukhari")

        assertTrue(result is HadithRefreshResult.Failure)
        assertTrue(
            (result as HadithRefreshResult.Failure).error is HadithLoadError.EmptyCollection
        )
    }

    @Test
    fun unknownKey_returnsUnknownCollectionWithoutNetworkCall() = runTest {
        val result = repository.refreshCollection("bogus")

        assertTrue(result is HadithRefreshResult.Failure)
        assertTrue(
            (result as HadithRefreshResult.Failure).error is HadithLoadError.UnknownCollection
        )
        assertTrue(api.requestedEditions.isEmpty())
    }

    @Test
    fun resync_replacesStaleRowsWithoutDuplicates() = runTest {
        api.handler = { FakeHadithApi.editionOf(benHadith(1, "v2")) }

        repository.refreshCollection("bukhari")
        repository.refreshCollection("bukhari")

        val cached = dao.getHadithsByCollection("bukhari").first()
        assertEquals(1, cached.size)
        assertEquals("v2", cached[0].banglaTranslation)
    }
}
