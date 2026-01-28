/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/9/2025
 */

@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.GoogleVolumeResponse
import aragones.sergio.readercollection.data.remote.model.STATES
import aragones.sergio.readercollection.data.remote.model.StateResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class BooksRemoteDataSourceTest {

    private val firebaseProvider: FirebaseProvider = mockk()
    private val dataSource = BooksRemoteDataSource(mockk(), firebaseProvider)

    @Test
    fun `GIVEN params without order and api success response WHEN search books is called THEN api is called without order param and return response`() =
        runTest {
            val query = "bookTitle"
            val page = 1
            val params = mutableMapOf(
                "q" to query,
                "startIndex" to "0",
                "maxResults" to "20",
                "key" to "apiKey",
            )
            val url = URLBuilder("/books/v1/volumes")
                .apply {
                    for (param in params) {
                        parameters.append(param.key, param.value)
                    }
                }.build()
                .encodedPathAndQuery
            val response = GoogleBookListResponse(
                totalItems = 0,
                items = emptyList(),
            )
            val mockEngine = MockEngine { request ->
                assertEquals(url, request.url.encodedPathAndQuery)
                respond(
                    content =
                        """
                        {
                          "kind": "books#volumes",
                          "totalItems": ${response.totalItems},
                          "items": [],
                          "unknown_key": "unknown_value"
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val client: HttpClient = getHttpClient(mockEngine)
            val dataSource = BooksRemoteDataSource(client, firebaseProvider)

            val result = dataSource.searchBooks(query, page, null)

            assertEquals(true, result.isSuccess)
            assertEquals(response, result.getOrNull())
        }

    @Test
    fun `GIVEN params with order and api success response WHEN search books is called THEN api is called with order param and return response`() =
        runTest {
            val query = "bookTitle"
            val page = 1
            val order = "newest"
            val params = mutableMapOf(
                "q" to query,
                "startIndex" to "0",
                "maxResults" to "20",
                "orderBy" to order,
                "key" to "apiKey",
            )
            val url = URLBuilder("/books/v1/volumes")
                .apply {
                    for (param in params) {
                        parameters.append(param.key, param.value)
                    }
                }.build()
                .encodedPathAndQuery
            val response = GoogleBookListResponse(
                totalItems = 0,
                items = emptyList(),
            )
            val mockEngine = MockEngine { request ->
                assertEquals(url, request.url.encodedPathAndQuery)
                respond(
                    content =
                        """
                        {
                          "kind": "books#volumes",
                          "totalItems": ${response.totalItems},
                          "items": [],
                          "unknown_key": "unknown_value"
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val client: HttpClient = getHttpClient(mockEngine)
            val dataSource = BooksRemoteDataSource(client, firebaseProvider)

            val result = dataSource.searchBooks(query, page, order)

            assertEquals(true, result.isSuccess)
            assertEquals(response, result.getOrNull())
        }

    @Test
    fun `GIVEN api failure response WHEN search books is called THEN return failure`() = runTest {
        val query = "bookTitle"
        val page = 1
        val order = "newest"
        val params = mutableMapOf(
            "q" to query,
            "startIndex" to "0",
            "maxResults" to "20",
            "orderBy" to order,
            "key" to "apiKey",
        )
        val url = URLBuilder("/books/v1/volumes")
            .apply {
                for (param in params) {
                    parameters.append(param.key, param.value)
                }
            }.build()
            .encodedPathAndQuery
        val error = HttpStatusCode(400, "Client Error")
        val mockEngine = MockEngine { request ->
            assertEquals(url, request.url.encodedPathAndQuery)
            respond(
                content = "{}",
                status = error,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val client: HttpClient = getHttpClient(mockEngine)
        val dataSource = BooksRemoteDataSource(client, firebaseProvider)

        val result = dataSource.searchBooks(query, page, order)

        assertEquals(true, result.isFailure)
        assertIs<IllegalStateException>(result.exceptionOrNull())
        assertEquals(
            "Unexpected status code response ${error.value} ${error.description}",
            result.exceptionOrNull()?.message,
        )
    }

    @Test
    fun `GIVEN book id and api success response WHEN get book is called THEN return response`() =
        runTest {
            val bookId = "bookId"
            val params = mapOf("key" to "apiKey")
            val url = URLBuilder("/books/v1/volumes/$bookId")
                .apply {
                    for (param in params) {
                        parameters.append(param.key, param.value)
                    }
                }.build()
                .encodedPathAndQuery
            val response = GoogleBookResponse(
                id = bookId,
                volumeInfo = GoogleVolumeResponse(
                    title = "",
                    subtitle = "",
                    authors = listOf(),
                    publisher = "",
                    publishedDate = null,
                    description = "",
                    industryIdentifiers = listOf(),
                    pageCount = 0,
                    categories = listOf(),
                    averageRating = 0.0,
                    ratingsCount = 0,
                    imageLinks = null,
                ),
            )
            val mockEngine = MockEngine { request ->
                assertEquals(url, request.url.encodedPathAndQuery)
                respond(
                    content =
                        """
                        {
                          "kind": "books#volume",
                          "id": "${response.id}",
                          "etag": "ZDiJO6z9ITU",
                          "selfLink": "https://www.googleapis.com/books/v1/volumes/FEWrDwAAQBAJ",
                          "volumeInfo": {
                            "title": "${response.volumeInfo.title}",
                            "subtitle": "${response.volumeInfo.subtitle}",
                            "authors": ${response.volumeInfo.authors},
                            "publisher": "${response.volumeInfo.publisher}",
                            "publishedDate": ${response.volumeInfo.publishedDate?.let { '"' + it.toString() + '"' }},
                            "description": "${response.volumeInfo.description}",
                            "industryIdentifiers": ${response.volumeInfo.industryIdentifiers},
                            "pageCount": ${response.volumeInfo.pageCount},
                            "categories": ${response.volumeInfo.categories},
                            "averageRating": ${response.volumeInfo.averageRating},
                            "ratingsCount": ${response.volumeInfo.ratingsCount},
                            "imageLinks": ${response.volumeInfo.imageLinks}
                          }
                        }
                        """.trimIndent(),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val client: HttpClient = getHttpClient(mockEngine)
            val dataSource = BooksRemoteDataSource(client, firebaseProvider)

            val result = dataSource.getBook(bookId)

            assertEquals(true, result.isSuccess)
            assertEquals(response, result.getOrNull())
        }

    @Test
    fun `GIVEN book id and api failure response WHEN get book is called THEN return failure`() =
        runTest {
            val bookId = "bookId"
            val params = mapOf("key" to "apiKey")
            val url = URLBuilder("/books/v1/volumes/$bookId")
                .apply {
                    for (param in params) {
                        parameters.append(param.key, param.value)
                    }
                }.build()
                .encodedPathAndQuery
            val error = HttpStatusCode(400, "Client Error")
            val mockEngine = MockEngine { request ->
                assertEquals(url, request.url.encodedPathAndQuery)
                respond(
                    content = "{}",
                    status = error,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
            val client: HttpClient = getHttpClient(mockEngine)
            val dataSource = BooksRemoteDataSource(client, firebaseProvider)

            val result = dataSource.getBook(bookId)

            assertEquals(true, result.isFailure)
            assertIs<IllegalStateException>(result.exceptionOrNull())
            assertEquals(
                "Unexpected status code response ${error.value} ${error.description}",
                result.exceptionOrNull()?.message,
            )
        }

    @Test
    fun `GIVEN success response and values for language WHEN fetch remote config values is called THEN formats and states are updated with new values`() {
        val language = "en"
        every { firebaseProvider.fetchRemoteConfigString("formats", any()) } answers {
            secondArg<(String) -> Unit>().invoke(getFormatsJson(language))
        }
        every { firebaseProvider.fetchRemoteConfigString("states", any()) } answers {
            secondArg<(String) -> Unit>().invoke(getStatesJson(language))
        }

        dataSource.fetchRemoteConfigValues(language)

        assertEquals(getFormats(), FORMATS)
        assertEquals(getStates(), STATES)
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("formats", any()) }
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("states", any()) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response and no values for language WHEN fetch remote config values is called THEN formats and states are empty`() {
        every { firebaseProvider.fetchRemoteConfigString("formats", any()) } answers {
            secondArg<(String) -> Unit>().invoke(getFormatsJson("en"))
        }
        every { firebaseProvider.fetchRemoteConfigString("states", any()) } answers {
            secondArg<(String) -> Unit>().invoke(getStatesJson("en"))
        }

        dataSource.fetchRemoteConfigValues("es")

        assertEquals(emptyList(), FORMATS)
        assertEquals(emptyList(), STATES)
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("formats", any()) }
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("states", any()) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN wrong response WHEN fetch remote config values is called THEN formats and states are empty`() {
        val language = "en"
        every { firebaseProvider.fetchRemoteConfigString("formats", any()) } answers {
            secondArg<(String) -> Unit>().invoke("values")
        }
        every { firebaseProvider.fetchRemoteConfigString("states", any()) } answers {
            secondArg<(String) -> Unit>().invoke("values")
        }

        dataSource.fetchRemoteConfigValues(language)

        assertEquals(emptyList(), FORMATS)
        assertEquals(emptyList(), STATES)
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("formats", any()) }
        verify(exactly = 1) { firebaseProvider.fetchRemoteConfigString("states", any()) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN get books THEN return list`() = runTest {
        val userId = "testUserId"
        val books = listOf(
            BookResponse(id = "1", title = "Title 1"),
            BookResponse(id = "2", title = "Title 2"),
        )
        val response = listOf(
            "1" to mapOf(
                "title" to "Title 1",
                "subtitle" to null,
                "authors" to null,
                "publisher" to null,
                "publishedDate" to null,
                "readingDate" to null,
                "description" to null,
                "summary" to null,
                "isbn" to null,
                "pageCount" to null,
                "categories" to null,
                "averageRating" to null,
                "ratingsCount" to null,
                "rating" to null,
                "thumbnail" to null,
                "image" to null,
                "format" to null,
                "state" to null,
                "priority" to null,
            ),
            "2" to mapOf(
                "title" to "Title 2",
                "subtitle" to null,
                "authors" to null,
                "publisher" to null,
                "publishedDate" to null,
                "readingDate" to null,
                "description" to null,
                "summary" to null,
                "isbn" to null,
                "pageCount" to null,
                "categories" to null,
                "averageRating" to null,
                "ratingsCount" to null,
                "rating" to null,
                "thumbnail" to null,
                "image" to null,
                "format" to null,
                "state" to null,
                "priority" to null,
            ),
        )
        coEvery { firebaseProvider.getBooks(any()) } returns response

        val result = dataSource.getBooks(userId)

        assertEquals(true, result.isSuccess)
        assertEquals(books, result.getOrNull())
        coVerify(exactly = 1) { firebaseProvider.getBooks(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN get books THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.getBooks(any()) } throws exception

        val result = dataSource.getBooks(userId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.getBooks(userId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response and existent friend and book WHEN get friend book THEN return book`() =
        runTest {
            val friendId = "testFriendId"
            val bookId = "bookId"
            val book = BookResponse(id = bookId, title = "Title")
            val response = mapOf(
                "title" to "Title",
                "subtitle" to null,
                "authors" to null,
                "publisher" to null,
                "publishedDate" to null,
                "readingDate" to null,
                "description" to null,
                "summary" to null,
                "isbn" to null,
                "pageCount" to null,
                "categories" to null,
                "averageRating" to null,
                "ratingsCount" to null,
                "rating" to null,
                "thumbnail" to null,
                "image" to null,
                "format" to null,
                "state" to null,
                "priority" to null,
            )
            coEvery { firebaseProvider.getBook(any(), any()) } returns response

            val result = dataSource.getFriendBook(friendId, bookId)

            assertEquals(true, result.isSuccess)
            assertEquals(book, result.getOrNull())
            coVerify(exactly = 1) { firebaseProvider.getBook(friendId, bookId) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN success response and non existent friend or book WHEN get friend book THEN return failure`() =
        runTest {
            val friendId = "testFriendId"
            val bookId = "bookId"
            coEvery { firebaseProvider.getBook(any(), any()) } returns mapOf()

            val result = dataSource.getFriendBook(friendId, bookId)

            assertEquals(true, result.isFailure)
            assertIs<NoSuchElementException>(result.exceptionOrNull())
            coVerify(exactly = 1) { firebaseProvider.getBook(friendId, bookId) }
            confirmVerified(firebaseProvider)
        }

    @Test
    fun `GIVEN failure response WHEN get friend book is called THEN return failure`() = runTest {
        val friendId = "testFriendId"
        val bookId = "bookId"
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.getBook(any(), any()) } throws exception

        val result = dataSource.getFriendBook(friendId, bookId)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.getBook(friendId, bookId) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN success response WHEN sync books THEN return success`() = runTest {
        val userId = "testUserId"
        val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
        val booksToRemove = listOf(BookResponse(id = "3"))
        coEvery { firebaseProvider.syncBooks(any(), any(), any()) } just Runs

        val result = dataSource.syncBooks(userId, booksToSave, booksToRemove)

        assertEquals(true, result.isSuccess)
        coVerify(exactly = 1) { firebaseProvider.syncBooks(userId, booksToSave, booksToRemove) }
        confirmVerified(firebaseProvider)
    }

    @Test
    fun `GIVEN failure response WHEN sync books THEN return failure`() = runTest {
        val userId = "testUserId"
        val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
        val booksToRemove = listOf(BookResponse(id = "3"))
        val exception = RuntimeException("Firestore error")
        coEvery { firebaseProvider.syncBooks(any(), any(), any()) } throws exception

        val result = dataSource.syncBooks(userId, booksToSave, booksToRemove)

        assertEquals(true, result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { firebaseProvider.syncBooks(userId, booksToSave, booksToRemove) }
        confirmVerified(firebaseProvider)
    }

    private fun getHttpClient(httpClientEngine: HttpClientEngine) = HttpClient(httpClientEngine) {
        engine {
            httpClientEngine.config
        }
    }.config {
        install(ContentNegotiation) {
            json(
                json = Json { ignoreUnknownKeys = true },
                contentType = ContentType.Application.Json,
            )
        }
        install(DefaultRequest) {
            url {
                protocol = URLProtocol.HTTPS
                host = "www.googleapis.com/books/v1"
                parameters.append("key", "apiKey")
            }
        }
    }

    private fun getFormatsJson(language: String): String = """{"$language":${getFormatsJson()}}"""

    private fun getFormatsJson(): String =
        """[{"id":"PHYSICAL","name":"Physical"},{"id":"DIGITAL","name":"Digital"}]"""

    private fun getFormats(): List<FormatResponse> = listOf(
        FormatResponse("PHYSICAL", "Physical"),
        FormatResponse("DIGITAL", "Digital"),
    )

    private fun getStatesJson(language: String): String = """{"$language":${getStatesJson()}}"""

    private fun getStatesJson(): String =
        """[{"id":"READING","name":"Reading"},{"id":"READ","name":"Read"}]"""

    private fun getStates(): List<StateResponse> = listOf(
        StateResponse("READING", "Reading"),
        StateResponse("READ", "Read"),
    )
}