/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.utils.Constants
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class BooksRemoteDataSource(
    private val client: HttpClient,
    private val firebaseProvider: FirebaseProvider,
) {

    //region Static properties
    companion object {
        private const val VOLUMES_PATH = "/volumes"
        private const val SEARCH_PARAM = "q"
        private const val PAGE_PARAM = "startIndex"
        private const val RESULTS_PARAM = "maxResults"
        private const val ORDER_PARAM = "orderBy"
        private const val RESULTS = 20
        private const val FORMATS_KEY = "formats"
        private const val STATES_KEY = "states"
    }
    //endregion

    //region Public methods
    suspend fun searchBooks(
        query: String,
        page: Int,
        order: String?,
    ): Result<GoogleBookListResponse> = try {
        val params = mutableMapOf(
            SEARCH_PARAM to query,
            PAGE_PARAM to ((page - 1) * RESULTS).toString(),
            RESULTS_PARAM to RESULTS.toString(),
        )
        if (order != null) {
            params[ORDER_PARAM] = order
        }

        val response = client.get(VOLUMES_PATH) {
            url {
                for (param in params) {
                    parameters.append(param.key, param.value)
                }
            }
        }
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }.mapCatching { response ->
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw IllegalStateException(
                "Unexpected status code response ${response.status}",
            )
        }
    }

    suspend fun getBook(volumeId: String): Result<GoogleBookResponse> = try {
        val response = client.get("$VOLUMES_PATH/$volumeId")
        Result.success(response)
    } catch (e: Exception) {
        Result.failure(e)
    }.mapCatching { response ->
        when (response.status) {
            HttpStatusCode.OK -> response.body()
            else -> throw IllegalStateException(
                "Unexpected status code response ${response.status}",
            )
        }
    }

    fun fetchRemoteConfigValues(language: String) {
        firebaseProvider.fetchRemoteConfigString(FORMATS_KEY) {
            setupFormats(it, language)
        }
        firebaseProvider.fetchRemoteConfigString(STATES_KEY) {
            setupStates(it, language)
        }
    }

    suspend fun getBooks(uuid: String): Result<List<BookResponse>> = runCatching {
        firebaseProvider.getBooks(uuid)
    }

    suspend fun getFriendBook(friendId: String, bookId: String): Result<BookResponse> =
        runCatching {
            val book = firebaseProvider.getBook(friendId, bookId)
            book ?: throw NoSuchElementException("Book not found")
        }

    suspend fun syncBooks(
        uuid: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
    ): Result<Unit> = runCatching {
        firebaseProvider.syncBooks(uuid, booksToSave, booksToRemove)
    }
    //endregion

    //region Private methods
    private fun setupFormats(formatsString: String, language: String) {
        if (formatsString.isNotEmpty()) {
            try {
                val languagedFormats =
                    Json
                        .parseToJsonElement(formatsString)
                        .jsonObject
                        .getValue(language)
                        .toString()
                Constants.FORMATS = Json
                    .decodeFromString<Array<FormatResponse>>(
                        languagedFormats,
                    ).asList()
            } catch (e: Exception) {
                println("BooksRemoteDataSource ${(e.message ?: "")}")
            }
        }
    }

    private fun setupStates(statesString: String, language: String) {
        if (statesString.isNotEmpty()) {
            try {
                val languagedStates =
                    Json
                        .parseToJsonElement(statesString)
                        .jsonObject
                        .getValue(language)
                        .toString()
                Constants.STATES = Json
                    .decodeFromString<Array<StateResponse>>(
                        languagedStates,
                    ).asList()
            } catch (e: Exception) {
                println("BooksRemoteDataSource ${(e.message ?: "")}")
            }
        }
    }
    //endregion
}