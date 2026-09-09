package com.islamic.bangla.presentation.viewmodel

import com.google.gson.JsonPrimitive
import com.islamic.bangla.data.FakeHadithApi
import com.islamic.bangla.data.FakeHadithDao
import com.islamic.bangla.data.model.Hadith
import com.islamic.bangla.data.preferences.SettingsStore
import com.islamic.bangla.data.remote.dto.HadithDto
import com.islamic.bangla.data.remote.dto.HadithEditionResponse
import com.islamic.bangla.data.remote.dto.HadithMetadataDto
import com.islamic.bangla.data.repository.HadithLoadError
import com.islamic.bangla.data.repository.HadithRepository
import java.net.UnknownHostException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

/**
 * Drives the real [HadithViewModel] + [HadithRepository] + [SettingsStore]
 * with a fake API/DAO: verifies the cache-fallback contract and that only a
 * genuine connectivity failure sets [HadithUiState.offline].
 *
 * Real dispatchers are used (SettingsStore/DataStore does real disk I/O),
 * and each test waits for a terminal UI state instead of guessing timing.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HadithViewModelTest {

    private lateinit var dao: FakeHadithDao
    private lateinit var api: FakeHadithApi

    @Before
    fun setup() {
        dao = FakeHadithDao()
        api = FakeHadithApi()
        Dispatchers.setMain(Dispatchers.Unconfined)
        // Fresh DataStore per test: last-sync timestamps must not leak.
        RuntimeEnvironment.getApplication().filesDir
            .resolve("datastore/settings.preferences_pb")
            .delete()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(): HadithViewModel = HadithViewModel(
        HadithRepository(dao, api),
        SettingsStore(RuntimeEnvironment.getApplication())
    )

    private fun edition(text: String) = HadithEditionResponse(
        metadata = HadithMetadataDto(),
        hadiths = listOf(HadithDto(hadithnumber = JsonPrimitive(1), text = text))
    )

    /** Waits until a refresh (or cache load) has fully settled. */
    private suspend fun awaitSettled(vm: HadithViewModel): HadithUiState =
        withTimeout(20_000) {
            vm.uiState.first {
                !it.isSyncing &&
                    (it.hadiths.isNotEmpty() || it.syncError != null || it.arabicMissing)
            }
        }

    @Test
    fun success_showsHadithsWithoutErrorFlags(): Unit = runBlocking {
        api.handler = { edition(text = if (it.startsWith("ben-")) "বাংলা" else "عربي") }

        val state = awaitSettled(viewModel())

        assertEquals(1, state.hadiths.size)
        assertEquals("বাংলা", state.hadiths[0].banglaTranslation)
        assertEquals("عربي", state.hadiths[0].arabicText)
        assertFalse(state.offline)
        assertFalse(state.arabicMissing)
        assertFalse(state.isSyncing)
        assertNull(state.syncError)
    }

    @Test
    fun offlineWithEmptyCache_reportsNoInternet(): Unit = runBlocking {
        api.handler = { throw UnknownHostException("no network") }

        val state = awaitSettled(viewModel())

        assertTrue(state.hadiths.isEmpty())
        assertTrue(state.offline)
        assertTrue(state.syncError is HadithLoadError.NoInternet)
        assertFalse(state.isSyncing)
    }

    @Test
    fun serverErrorWithCache_keepsCacheAndStaysOnline(): Unit = runBlocking {
        dao.insertHadith(
            Hadith(
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
        api.handler = {
            throw HttpException(Response.error<Any>(503, "down".toResponseBody()))
        }

        val state = awaitSettled(viewModel())

        // Cache fallback: cached hadith stays visible…
        assertEquals(1, state.hadiths.size)
        // …and a server error must NOT be reported as "no internet".
        assertFalse(state.offline)
        val error = state.syncError
        assertTrue(error is HadithLoadError.ServerError)
        assertEquals(503, (error as HadithLoadError.ServerError).httpCode)
    }

    @Test
    fun arabicFailure_showsBanglaWithArabicMissingFlag(): Unit = runBlocking {
        api.handler = { editionName ->
            if (editionName.startsWith("ben-")) edition("বাংলা") else throw UnknownHostException()
        }

        val state = awaitSettled(viewModel())

        assertEquals(1, state.hadiths.size)
        assertTrue(state.arabicMissing)
        assertFalse(state.offline)
        assertNull(state.syncError)
    }

    @Test
    fun retryAfterFailure_recovers(): Unit = runBlocking {
        var attempts = 0
        api.handler = {
            attempts++
            if (attempts == 1) throw UnknownHostException() else edition("বাংলা")
        }

        val vm = viewModel()
        assertTrue(awaitSettled(vm).offline)

        vm.selectCollection("bukhari")
        val state = awaitSettled(vm)

        assertFalse(state.offline)
        assertNull(state.syncError)
        assertEquals(1, state.hadiths.size)
    }
}
