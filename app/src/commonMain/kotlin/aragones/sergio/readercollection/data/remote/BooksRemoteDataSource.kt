/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.ALL_FORMATS
import aragones.sergio.readercollection.data.remote.model.ALL_GENRES
import aragones.sergio.readercollection.data.remote.model.ALL_STATES
import aragones.sergio.readercollection.data.remote.model.BaseModel
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.data.remote.model.GENRES
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.STATES
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
        private const val GENRES_KEY = "genres"
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
            ALL_FORMATS = parseValues(it)
            FORMATS = ALL_FORMATS[language] ?: emptyList()
        }
        firebaseProvider.fetchRemoteConfigString(GENRES_KEY) {
            ALL_GENRES = parseValues(it)
            GENRES = ALL_GENRES[language] ?: emptyList()
        }
        firebaseProvider.fetchRemoteConfigString(STATES_KEY) {
            ALL_STATES = parseValues(it)
            STATES = ALL_STATES[language] ?: emptyList()
        }
    }

    suspend fun getBooks(uuid: String): Result<List<BookResponse>> = runCatching {
        firebaseProvider.getBooks(uuid).mapNotNull { it.second.toBook(it.first) }
    }

    suspend fun getFriendBook(friendId: String, bookId: String): Result<BookResponse> =
        runCatching {
            val book = firebaseProvider.getBook(friendId, bookId).toBook(bookId)
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
    private inline fun <reified T : BaseModel<String>> parseValues(
        values: String,
    ): Map<String, List<T>> = if (values.isNotEmpty()) {
        try {
            val valuesJson = Json
                .parseToJsonElement(values)
                .jsonObject
                .toString()
            Json
                .decodeFromString<Map<String, Array<T>>>(valuesJson)
                .mapValues { it.value.asList() }
        } catch (e: Exception) {
            println("BooksRemoteDataSource ${(e.message ?: "")}")
            emptyMap()
        }
    } else {
        emptyMap()
    }
    //endregion
}