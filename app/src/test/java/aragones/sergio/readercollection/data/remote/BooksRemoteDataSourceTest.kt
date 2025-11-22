/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/9/2025
 */

@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.BuildConfig
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.StateResponse
import aragones.sergio.readercollection.data.remote.services.GoogleApiService
import aragones.sergio.readercollection.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import io.mockk.EqMatcher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class BooksRemoteDataSourceTest {

    private val googleApiService: GoogleApiService = mockk()
    private val remoteConfig: FirebaseRemoteConfig = mockk(relaxed = true)
    private val firestore: FirebaseFirestore = mockk()
    private val dataSource = BooksRemoteDataSource(googleApiService, remoteConfig, firestore)

    @Test
    fun `GIVEN params without order and api success response WHEN search books is called THEN api is called without order param and return response`() =
        runTest {
            val query = "bookTitle"
            val page = 1
            val params = mutableMapOf(
                "key" to BuildConfig.API_KEY,
                "q" to query,
                "startIndex" to "0",
                "maxResults" to "20",
            )
            val response = mockk<GoogleBookListResponse>()
            coEvery { googleApiService.searchGoogleBooks(params) } returns response

            val result = dataSource.searchBooks(query, page, null)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(response, result.getOrNull())
            coVerify(exactly = 1) { googleApiService.searchGoogleBooks(params) }
            confirmVerified(googleApiService)
        }

    @Test
    fun `GIVEN params with order and api success response WHEN search books is called THEN api is called with order param and return response`() =
        runTest {
            val query = "bookTitle"
            val page = 1
            val order = "newest"
            val params = mutableMapOf(
                "key" to BuildConfig.API_KEY,
                "q" to query,
                "startIndex" to "0",
                "maxResults" to "20",
                "orderBy" to order,
            )
            val response = mockk<GoogleBookListResponse>()
            coEvery { googleApiService.searchGoogleBooks(params) } returns response

            val result = dataSource.searchBooks(query, page, order)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(response, result.getOrNull())
            coVerify(exactly = 1) { googleApiService.searchGoogleBooks(params) }
            confirmVerified(googleApiService)
        }

    @Test
    fun `GIVEN api failure response WHEN search books is called THEN return failure`() = runTest {
        val query = "bookTitle"
        val page = 1
        val order = "newest"
        val params = mutableMapOf(
            "key" to BuildConfig.API_KEY,
            "q" to query,
            "startIndex" to "0",
            "maxResults" to "20",
            "orderBy" to order,
        )
        val exception = HttpException(Response.error<Any>(400, mockk(relaxed = true)))
        coEvery { googleApiService.searchGoogleBooks(params) } throws exception

        val result = dataSource.searchBooks(query, page, order)

        Assert.assertEquals(true, result.isFailure)
        Assert.assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { googleApiService.searchGoogleBooks(params) }
        confirmVerified(googleApiService)
    }

    @Test
    fun `GIVEN book id and api success response WHEN get book is called THEN return response`() =
        runTest {
            val bookId = "bookId"
            val params = mapOf("key" to BuildConfig.API_KEY)
            val response = mockk<GoogleBookResponse>()
            coEvery { googleApiService.getGoogleBook(bookId, params) } returns response

            val result = dataSource.getBook(bookId)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(response, result.getOrNull())
            coVerify(exactly = 1) { googleApiService.getGoogleBook(bookId, params) }
            confirmVerified(googleApiService)
        }

    @Test
    fun `GIVEN book id and api failure response WHEN get book is called THEN return failure`() =
        runTest {
            val bookId = "bookId"
            val params = mapOf("key" to BuildConfig.API_KEY)
            val exception = HttpException(Response.error<Any>(400, mockk(relaxed = true)))
            coEvery { googleApiService.getGoogleBook(bookId, params) } throws exception

            val result = dataSource.getBook(bookId)

            Assert.assertEquals(true, result.isFailure)
            Assert.assertEquals(exception, result.exceptionOrNull())
            coVerify(exactly = 1) { googleApiService.getGoogleBook(bookId, params) }
            confirmVerified(googleApiService)
        }

    @Test
    fun `GIVEN success response and values for language WHEN fetch remote config values is called THEN formats and states are updated with new values`() {
        val language = "en"
        givenRemoteConfigFetchSuccess(getFormatsJson(), getStatesJson(), language)

        dataSource.fetchRemoteConfigValues(language)

        Assert.assertEquals(getFormats(), Constants.FORMATS)
        Assert.assertEquals(getStates(), Constants.STATES)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString("formats") }
        verify(exactly = 2) { remoteConfig.getString("states") }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN success response and no values for language WHEN fetch remote config values is called THEN formats and states are erased`() {
        val language = "en"
        val formatsJson = getFormatsJson()
        val statesJson = getStatesJson()
        givenRemoteConfigFetchSuccess(formatsJson, statesJson, language)

        dataSource.fetchRemoteConfigValues("es")

        Assert.assertEquals(0, Constants.FORMATS.size)
        Assert.assertEquals(0, Constants.STATES.size)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString("formats") }
        verify(exactly = 2) { remoteConfig.getString("states") }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN wrong response WHEN fetch remote config values is called THEN formats and states are not updated`() {
        val language = "en"
        val currentFormats = Constants.FORMATS
        val currentStates = Constants.STATES
        givenRemoteConfigFetchFailure("", "", language)

        dataSource.fetchRemoteConfigValues(language)

        Assert.assertEquals(currentFormats, Constants.FORMATS)
        Assert.assertEquals(currentStates, Constants.STATES)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString("formats") }
        verify(exactly = 2) { remoteConfig.getString("states") }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN failure response WHEN fetch remote config values is called THEN formats and states are updated with default values`() {
        val language = "en"
        val formatsJson = getFormatsJson(language)
        val statesJson = getStatesJson(language)
        givenRemoteConfigFetchFailure(formatsJson, statesJson, language)

        dataSource.fetchRemoteConfigValues(language)

        Assert.assertEquals(getFormats(), Constants.FORMATS)
        Assert.assertEquals(getStates(), Constants.STATES)
        verify(exactly = 1) { remoteConfig.fetchAndActivate() }
        verify(exactly = 2) { remoteConfig.getString("formats") }
        verify(exactly = 2) { remoteConfig.getString("states") }
        confirmVerified(remoteConfig)
    }

    @Test
    fun `GIVEN success response and uuid with books WHEN get books is called THEN return list`() =
        runTest {
            val userId = "testUserId"
            val books = listOf(BookResponse(id = "1"), BookResponse(id = "2"))
            givenGetBooksSuccess(userId, books)

            val result = dataSource.getBooks(userId)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(books, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and uuid without books WHEN get books is called THEN return empty list`() =
        runTest {
            val userId = "testUserId"
            val books = emptyList<BookResponse>()
            givenGetBooksSuccess(userId, books)

            val result = dataSource.getBooks(userId)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(books, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get books is called THEN return failure`() = runTest {
        val userId = "testUserId"
        val exception = RuntimeException("Firestore error")
        givenGetBooksFailure(userId, exception)

        val result = dataSource.getBooks(userId)

        Assert.assertEquals(true, result.isFailure)
        Assert.assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and existent friend and book WHEN get friend book is called THEN return book`() =
        runTest {
            val friendId = "testFriendId"
            val bookId = "bookId"
            val book = BookResponse(id = bookId)
            givenGetBookSuccess(friendId, bookId, book)

            val result = dataSource.getFriendBook(friendId, bookId)

            Assert.assertEquals(true, result.isSuccess)
            Assert.assertEquals(book, result.getOrNull())
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN success response and non existent friend or book WHEN get friend book is called THEN return failure`() =
        runTest {
            val friendId = "testFriendId"
            val bookId = "bookId"
            givenGetBookSuccess(friendId, bookId, null)

            val result = dataSource.getFriendBook(friendId, bookId)

            Assert.assertEquals(true, result.isFailure)
            Assert.assertEquals(
                NoSuchElementException::class.java,
                result.exceptionOrNull()?.javaClass,
            )
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
        }

    @Test
    fun `GIVEN failure response WHEN get friend book is called THEN return failure`() = runTest {
        val friendId = "testFriendId"
        val bookId = "bookId"
        val exception = RuntimeException("Firestore error")
        givenGetBookFailure(friendId, bookId, exception)

        val result = dataSource.getFriendBook(friendId, bookId)

        Assert.assertEquals(true, result.isFailure)
        Assert.assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
    }

    @Test
    fun `GIVEN success response and books to save and books to remove WHEN sync books is called THEN set is called and delete is called and return success`() =
        runTest {
            val userId = "testUserId"
            val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
            val booksToRemove = listOf(BookResponse(id = "3"))
            val batch = mockk<WriteBatch>()
            givenSyncBooksSuccess(userId, booksToSave, booksToRemove, batch)

            val result = dataSource.syncBooks(userId, booksToSave, booksToRemove)

            Assert.assertEquals(true, result.isSuccess)
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
            verify(exactly = 2) { batch.set(any(), any()) }
            verify(exactly = 1) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch)
        }

    @Test
    fun `GIVEN success response without books to save nor books to remove WHEN sync books is called THEN set and delete are not called and return success`() =
        runTest {
            val userId = "testUserId"
            val booksToSave = emptyList<BookResponse>()
            val booksToRemove = emptyList<BookResponse>()
            val batch = mockk<WriteBatch>()
            givenSyncBooksSuccess(userId, booksToSave, booksToRemove, batch)

            val result = dataSource.syncBooks(userId, booksToSave, booksToRemove)

            Assert.assertEquals(true, result.isSuccess)
            verify(exactly = 1) { firestore.batch() }
            verify(exactly = 1) { firestore.collection("users") }
            confirmVerified(firestore)
            verify(exactly = 0) { batch.set(any(), any()) }
            verify(exactly = 0) { batch.delete(any()) }
            verify(exactly = 1) { batch.commit() }
            confirmVerified(batch)
        }

    @Test
    fun `GIVEN failure response WHEN sync books is called THEN return failure`() = runTest {
        val userId = "testUserId"
        val booksToSave = listOf(BookResponse(id = "1"), BookResponse("2"))
        val booksToRemove = listOf(BookResponse(id = "3"))
        val batch = mockk<WriteBatch>()
        val exception = RuntimeException("Firestore error")
        givenSyncBooksFailure(userId, booksToSave, booksToRemove, batch, exception)

        val result = dataSource.syncBooks(userId, booksToSave, booksToRemove)

        Assert.assertEquals(true, result.isFailure)
        Assert.assertEquals(exception, result.exceptionOrNull())
        verify(exactly = 1) { firestore.batch() }
        verify(exactly = 1) { firestore.collection("users") }
        confirmVerified(firestore)
        verify(exactly = 2) { batch.set(any(), any()) }
        verify(exactly = 1) { batch.delete(any()) }
        verify(exactly = 1) { batch.commit() }
        confirmVerified(batch)
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

    private fun givenRemoteConfigFetchSuccess(formats: String, states: String, language: String) {
        mockkConstructor(JSONObject::class)
        every {
            constructedWith<JSONObject>(
                EqMatcher(getFormatsJson(language)),
            ).get(language).toString()
        } returns formats
        every {
            constructedWith<JSONObject>(EqMatcher(getStatesJson(language))).get(language).toString()
        } returns states
        every { remoteConfig.getString("formats") } returns getFormatsJson(language)
        every { remoteConfig.getString("states") } returns getStatesJson(language)
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        val task = mockk<Task<Boolean>>()
        every { task.addOnCompleteListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onComplete(
                mockk {
                    every { isSuccessful } returns true
                },
            )
            task
        }
        every { remoteConfig.fetchAndActivate() } returns task
    }

    private fun givenRemoteConfigFetchFailure(formats: String, states: String, language: String) {
        mockkConstructor(JSONObject::class)
        every {
            constructedWith<JSONObject>(
                EqMatcher(getFormatsJson(language)),
            ).get(language).toString()
        } returns getFormatsJson()
        every {
            constructedWith<JSONObject>(EqMatcher(getStatesJson(language))).get(language).toString()
        } returns getStatesJson()
        every { remoteConfig.getString("formats") } returns formats
        every { remoteConfig.getString("states") } returns states
        val task = mockk<Task<Boolean>>()
        val listenerSlot = slot<OnCompleteListener<Boolean>>()
        every { task.addOnCompleteListener(capture(listenerSlot)) } answers {
            listenerSlot.captured.onComplete(
                mockk {
                    every { isSuccessful } returns true
                },
            )
            task
        }
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns RuntimeException("Firestore error")
        every { remoteConfig.fetchAndActivate() } returns task
    }

    private fun getBooks(userId: String): CollectionReference {
        val usersCollectionReference = mockk<CollectionReference>()
        val userDocumentReference = mockk<DocumentReference>()
        val booksCollectionReference = mockk<CollectionReference>()
        every { firestore.collection("users") } returns usersCollectionReference
        every { usersCollectionReference.document(userId) } returns userDocumentReference
        every { userDocumentReference.collection("books") } returns booksCollectionReference
        return booksCollectionReference
    }

    private fun givenGetBooksSuccess(userId: String, books: List<BookResponse>) {
        val collectionReference = getBooks(userId)
        val task = mockk<Task<QuerySnapshot>>()
        val querySnapshot = mockk<QuerySnapshot>()
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns querySnapshot
        every { querySnapshot.toObjects(BookResponse::class.java) } returns books
    }

    private fun givenGetBooksFailure(userId: String, exception: Exception) {
        val collectionReference = getBooks(userId)
        val task = mockk<Task<QuerySnapshot>>()
        every { collectionReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenGetBookSuccess(friendId: String, bookId: String, book: BookResponse?) {
        val collectionReference = getBooks(friendId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        val documentSnapshot = mockk<DocumentSnapshot>()
        every { collectionReference.document(bookId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns null
        every { task.result } returns documentSnapshot
        every { documentSnapshot.toObject(BookResponse::class.java) } returns book
    }

    private fun givenGetBookFailure(friendId: String, bookId: String, exception: Exception) {
        val collectionReference = getBooks(friendId)
        val documentReference = mockk<DocumentReference>()
        val task = mockk<Task<DocumentSnapshot>>()
        every { collectionReference.document(bookId) } returns documentReference
        every { documentReference.get() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }

    private fun givenSetBooksSuccess(
        books: List<BookResponse>,
        booksRef: CollectionReference,
        batch: WriteBatch,
    ) {
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { booksRef.document(book.id) } returns bookDocumentReference
            every { batch.set(bookDocumentReference, book) } returns mockk()
        }
    }

    private fun givenDeleteBooksSuccess(
        books: List<BookResponse>,
        booksRef: CollectionReference,
        batch: WriteBatch,
    ) {
        books.forEach { book ->
            val bookDocumentReference = mockk<DocumentReference>()
            every { booksRef.document(book.id) } returns bookDocumentReference
            every { batch.delete(bookDocumentReference) } returns mockk()
        }
    }

    private fun givenSyncBooksSuccess(
        userId: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
        batch: WriteBatch,
    ) {
        every { firestore.batch() } returns batch
        val collectionReference = getBooks(userId)
        givenSetBooksSuccess(booksToSave, collectionReference, batch)
        givenDeleteBooksSuccess(booksToRemove, collectionReference, batch)
        every { batch.commit() } returns Tasks.forResult(mockk<Void>())
    }

    private fun givenSyncBooksFailure(
        userId: String,
        booksToSave: List<BookResponse>,
        booksToRemove: List<BookResponse>,
        batch: WriteBatch,
        exception: Exception,
    ) {
        every { firestore.batch() } returns batch
        val collectionReference = getBooks(userId)
        givenSetBooksSuccess(booksToSave, collectionReference, batch)
        givenDeleteBooksSuccess(booksToRemove, collectionReference, batch)
        val task = mockk<Task<Void>>()
        every { batch.commit() } returns task
        every { task.isComplete } returns true
        every { task.isCanceled } returns false
        every { task.exception } returns exception
    }
}