package com.islamic.bangla.data.repository

import com.google.gson.JsonParseException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import retrofit2.HttpException

/**
 * Typed failures for a Hadith collection refresh.
 *
 * Previously every failure (server error, malformed payload, unknown key, …)
 * surfaced as a generic "no internet" message even on a working connection.
 * These types let the ViewModel/UI tell the user what actually went wrong.
 */
sealed interface HadithLoadError {
    /** Device cannot reach the CDN (DNS / connect / idle-read timeout). */
    data object NoInternet : HadithLoadError

    /** CDN reachable but returned an HTTP error status. */
    data class ServerError(val httpCode: Int) : HadithLoadError

    /** Payload downloaded but could not be parsed into hadiths. */
    data object DataFormatError : HadithLoadError

    /** Requested collection key is not a known hadith-api edition. */
    data object UnknownCollection : HadithLoadError

    /** Edition downloaded but contains zero hadiths. */
    data object EmptyCollection : HadithLoadError

    /** Anything else (DB failure, unexpected runtime error, …). */
    data class Unexpected(val message: String?) : HadithLoadError
}

/**
 * Outcome of [HadithRepository.refreshCollection].
 *
 * Arabic is best-effort: when only the Arabic edition fails, the Bangla
 * hadiths are still cached and reported via [Success] with
 * [Success.arabicComplete] == false instead of failing the whole refresh.
 */
sealed interface HadithRefreshResult {
    data class Success(
        val arabicComplete: Boolean,
        val arabicError: HadithLoadError? = null
    ) : HadithRefreshResult

    data class Failure(val error: HadithLoadError) : HadithRefreshResult
}

/**
 * Maps any refresh exception to a precise [HadithLoadError].
 *
 * The whole cause chain is inspected because Retrofit/Gson wrap the root
 * cause (e.g. a mid-download stall surfaces as JsonParseException caused by
 * SocketTimeoutException — that is still [HadithLoadError.NoInternet]).
 */
fun Throwable.toHadithLoadError(): HadithLoadError {
    var cause: Throwable? = this
    var httpCode: Int? = null
    var sawDataError = false
    while (cause != null) {
        when (cause) {
            is UnknownHostException,
            is ConnectException,
            is SocketTimeoutException,
            is NoRouteToHostException -> return HadithLoadError.NoInternet
            is HttpException -> if (httpCode == null) httpCode = cause.code()
            is JsonParseException -> sawDataError = true
        }
        cause = cause.cause
    }
    if (httpCode != null) return HadithLoadError.ServerError(httpCode)
    if (sawDataError) return HadithLoadError.DataFormatError
    return HadithLoadError.Unexpected(message)
}
