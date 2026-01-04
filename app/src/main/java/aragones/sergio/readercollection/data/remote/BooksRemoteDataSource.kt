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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class BooksRemoteDataSource(
    private val client: HttpClient,
    private val remoteConfig: FirebaseRemoteConfig,
    private val firestore: FirebaseFirestore,
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
        private const val BOOKS_PATH = "books"
        private const val USERS_PATH = "users"
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
        setupFormats(remoteConfig.getString(FORMATS_KEY), language)
        setupStates(remoteConfig.getString(STATES_KEY), language)

        remoteConfig.fetchAndActivate().addOnCompleteListener {
            setupFormats(remoteConfig.getString(FORMATS_KEY), language)
            setupStates(remoteConfig.getString(STATES_KEY), language)
        }
    }

    suspend fun getBooks(uuid: String): Result<List<BookResponse>> = runCatching {
        firestore
            .collection(USERS_PATH)
            .document(uuid)
            .collection(BOOKS_PATH)
            .get()
            .await()
            .mapNotNull { it.toMap().toBook(it.id) }
    }

    suspend fun getFriendBook(friendId: String, bookId: String): Result<BookResponse> =
        runCatching {
            val document = firestore
                .collection(USERS_PATH)
                .document(friendId)
                .collection(BOOKS_PATH)
                .document(bookId)
                .get()
                .await()
            document.toMap().toBook(document.id) ?: throw NoSuchElementException("Book not found")
        }

    @OptIn(ExperimentalTime::class)
    suspend fun syncBooks(
        uuid: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
    ): Result<Unit> = runCatching {
        val batch = firestore.batch()
        val booksRef = firestore
            .collection(USERS_PATH)
            .document(uuid)
            .collection(BOOKS_PATH)

        booksToSave.forEach { book ->
            val docRef = booksRef.document(book.id)
            val values = book.toMap().toMutableMap()
            values["publishedDate"] = (values["publishedDate"] as? Instant)?.toTimestamp()
            values["readingDate"] = (values["readingDate"] as? Instant)?.toTimestamp()
            batch.set(docRef, values)
        }

        booksToRemove.forEach { book ->
            val docRef = booksRef.document(book.id)
            batch.delete(docRef)
        }

        batch.commit().await()
    }
    //endregion

    //region Private methods
    private fun setupFormats(formatsString: String, language: String) {
        if (formatsString.isNotEmpty()) {
            var formats = listOf<FormatResponse>()
            try {
                val languagedFormats =
                    Json
                        .parseToJsonElement(formatsString)
                        .jsonObject
                        .getValue(language)
                        .toString()
                formats = Json.decodeFromString<Array<FormatResponse>>(languagedFormats).asList()
            } catch (e: Exception) {
                println("BooksRemoteDataSource ${(e.message ?: "")}")
            }
            Constants.FORMATS = formats
        }
    }

    private fun setupStates(statesString: String, language: String) {
        if (statesString.isNotEmpty()) {
            var states = listOf<StateResponse>()
            try {
                val languagedStates =
                    Json
                        .parseToJsonElement(statesString)
                        .jsonObject
                        .getValue(language)
                        .toString()
                states = Json.decodeFromString<Array<StateResponse>>(languagedStates).asList()
            } catch (e: Exception) {
                println("BooksRemoteDataSource ${(e.message ?: "")}")
            }
            Constants.STATES = states
        }
    }
    //endregion

    //region Public methods
    @OptIn(ExperimentalTime::class)
    private fun DocumentSnapshot.toMap(): Map<String, Any?> = mapOf(
        "title" to getString("title"),
        "subtitle" to getString("subtitle"),
        "authors" to get("authors"),
        "publisher" to getString("publisher"),
        "publishedDate" to (get("publishedDate") as? Timestamp).toInstant(),
        "readingDate" to (get("readingDate") as? Timestamp).toInstant(),
        "description" to getString("description"),
        "summary" to getString("summary"),
        "isbn" to getString("isbn"),
        "pageCount" to get("pageCount"),
        "categories" to get("categories"),
        "averageRating" to getDouble("averageRating"),
        "ratingsCount" to get("ratingsCount"),
        "rating" to getDouble("rating"),
        "thumbnail" to getString("thumbnail"),
        "image" to getString("image"),
        "format" to getString("format"),
        "state" to getString("state"),
        "priority" to get("priority"),
    )

    @OptIn(ExperimentalTime::class)
    private fun Timestamp?.toInstant(): Instant? = this?.let {
        Instant.fromEpochSeconds(it.seconds, it.nanoseconds)
    }

    @OptIn(ExperimentalTime::class)
    private fun Instant?.toTimestamp(): Timestamp? = this?.let {
        Timestamp(it.epochSeconds, it.nanosecondsOfSecond)
    }
    //endregion
}