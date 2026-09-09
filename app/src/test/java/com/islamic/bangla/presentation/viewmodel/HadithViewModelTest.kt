package com.islamic.bangla.presentation.viewmodel

import androidx.test.core.app.ApplicationProvider
import com.google.gson.JsonPrimitive
import com.islamic.bangla.MainDispatcherRule
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

/**
 * Drives the real [HadithViewModel] + [HadithRepository] + [SettingsStore]
 * with a fake API/DAO: verifies the cache-fallback contract and that only a
 * genuine connectivity failure sets [HadithUiState.offline].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class HadithViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var dao: FakeHadithDao
    private lateinit var api: FakeHadithApi

    @Before
    fun setup() {
        dao = FakeHadithDao()
        api = FakeHadithApi()
    }

    private fun viewModel(): HadithViewModel = HadithViewModel(
        HadithRepository(dao, api),
        SettingsStore(RuntimeEnvironment.getApplication())
    )

    private fun edition(text: String) = HadithEditionResponse(
        metadata = HadithMetadataDto(),
        hadiths = listOf(HadithDto(hadithnumber = JsonPrimitive(1), text = text))
    )

    @Test
    fun success_showsHadithsWithoutErrorFlags() = runTest {
        api.handler = { edition(text = if (it.startsWith("ben-")) "বাংলা" else "عربي") }

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.hadiths.size)
        assertEquals("বাংলা", state.hadiths[0].banglaTranslation)
        assertEquals("عربي", state.hadiths[0].arabicText)
        assertFalse(state.offline)
        assertFalse(state.arabicMissing)
        assertFalse(state.isSyncing)
        assertNull(state.syncError)
    }

    @Test
    fun offlineWithEmptyCache_reportsNoInternet() = runTest {
        api.handler = { throw UnknownHostException("no network") }

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertTrue(state.hadiths.isEmpty())
        assertTrue(state.offline)
        assertTrue(state.syncError is HadithLoadError.NoInternet)
        assertFalse(state.isSyncing)
    }

    @Test
    fun serverErrorWithCache_keepsCacheAndStaysOnline() = runTest {
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

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        // Cache fallback: cached hadith stays visible…
        assertEquals(1, state.hadiths.size)
        // …and a server error must NOT be reported as "no internet".
        assertFalse(state.offline)
        val error = state.syncError
        assertTrue(error is HadithLoadError.ServerError)
        assertEquals(503, (error as HadithLoadError.ServerError).httpCode)
    }

    @Test
    fun arabicFailure_showsBanglaWithArabicMissingFlag() = runTest {
        api.handler = { editionName ->
            if (editionName.startsWith("ben-")) edition("বাংলা") else throw UnknownHostException()
        }

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(1, state.hadiths.size)
        assertTrue(state.arabicMissing)
        assertFalse(state.offline)
        assertNull(state.syncError)
    }

    @Test
    fun retryAfterFailure_recovers() = runTest {
        var attempts = 0
        api.handler = {
            attempts++
            if (attempts == 1) throw UnknownHostException() else edition("বাংলা")
        }

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.offline)

        vm.selectCollection("bukhari")
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse(state.offline)
        assertNull(state.syncError)
        assertEquals(1, state.hadiths.size)
    }
}
