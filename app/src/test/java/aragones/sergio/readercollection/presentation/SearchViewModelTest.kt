/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 26/9/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.GoogleBookListResponse
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.GoogleVolumeResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.search.SearchUiState
import aragones.sergio.readercollection.presentation.search.SearchViewModel
import aragones.sergio.readercollection.presentation.utils.MainDispatcherRule
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.BookState
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()

    private val viewModel = SearchViewModel(
        BooksRepository(
            booksLocalDataSource,
            booksRemoteDataSource,
            mainDispatcherRule.testDispatcher,
        ),
    )

    @Test
    fun `WHEN onResume THEN saved books are fetched`() = runTest {
        every { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())

        viewModel.onResume()

        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN success response WHEN searchBooks THEN return Success state with searched books`() =
        runTest {
            val bookId = "bookId"
            givenSearchBooksSuccess(bookId)
            val expectedBooks = listOf(
                getDefaultGoogleBook(bookId).toDomain(),
                Book(""),
            )

            viewModel.state.test {
                Assert.assertEquals(SearchUiState.Empty, awaitItem())

                viewModel.searchBooks()

                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = true,
                        query = "",
                        books = Books(),
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = false,
                        query = "",
                        books = Books(expectedBooks),
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksRemoteDataSource.searchBooks("", 1, null) }
            confirmVerified(booksRemoteDataSource)
        }

    @Test
    fun `GIVEN empty response WHEN searchBooks THEN return Success state with empty list`() =
        runTest {
            coEvery {
                booksRemoteDataSource.searchBooks(any(), any(), any())
            } returns Result.success(
                GoogleBookListResponse(
                    totalItems = 0,
                    items = emptyList(),
                ),
            )

            viewModel.state.test {
                Assert.assertEquals(SearchUiState.Empty, awaitItem())

                viewModel.searchBooks()

                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = true,
                        query = "",
                        books = Books(),
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = false,
                        query = "",
                        books = Books(),
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksRemoteDataSource.searchBooks("", 1, null) }
            confirmVerified(booksRemoteDataSource)
        }

    @Test
    fun `GIVEN failure response WHEN searchBooks THEN return Success state with empty list`() =
        runTest {
            coEvery {
                booksRemoteDataSource.searchBooks(any(), any(), any())
            } returns Result.failure(RuntimeException("Firestore error"))

            viewModel.state.test {
                Assert.assertEquals(SearchUiState.Empty, awaitItem())

                viewModel.searchBooks()

                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = true,
                        query = "",
                        books = Books(),
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    SearchUiState.Success(
                        isLoading = false,
                        query = "",
                        books = Books(),
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksRemoteDataSource.searchBooks("", 1, null) }
            confirmVerified(booksRemoteDataSource)
        }

    @Test
    fun `GIVEN reload WHEN searchBooks THEN page and books are reset`() = runTest {
        val bookId = "bookId"
        givenSearchBooksSuccess(bookId)

        viewModel.state.test {
            Assert.assertEquals(SearchUiState.Empty, awaitItem())

            viewModel.searchBooks()

            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = true,
                    query = "",
                    books = Books(),
                ),
                awaitItem(),
            )
            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = false,
                    query = "",
                    books = Books(
                        listOf(
                            getDefaultGoogleBook(bookId).toDomain(),
                            Book(""),
                        ),
                    ),
                ),
                awaitItem(),
            )

            viewModel.searchBooks()

            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = true,
                    query = "",
                    books = Books(
                        listOf(
                            getDefaultGoogleBook(bookId).toDomain(),
                            Book(""),
                        ),
                    ),
                ),
                awaitItem(),
            )
            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = false,
                    query = "",
                    books = Books(
                        listOf(
                            getDefaultGoogleBook(bookId).toDomain(),
                            getDefaultGoogleBook(bookId).toDomain(),
                            Book(""),
                        ),
                    ),
                ),
                awaitItem(),
            )

            viewModel.searchBooks(reload = true)

            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = true,
                    query = "",
                    books = Books(
                        listOf(
                            getDefaultGoogleBook(bookId).toDomain(),
                            getDefaultGoogleBook(bookId).toDomain(),
                            Book(""),
                        ),
                    ),
                ),
                awaitItem(),
            )
            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = false,
                    query = "",
                    books = Books(
                        listOf(
                            getDefaultGoogleBook(bookId).toDomain(),
                            Book(""),
                        ),
                    ),
                ),
                awaitItem(),
            )
        }
        coVerify(exactly = 2) { booksRemoteDataSource.searchBooks("", 1, null) }
        coVerify { booksRemoteDataSource.searchBooks("", 2, null) }
        confirmVerified(booksRemoteDataSource)
    }

    @Test
    fun `GIVEN new query WHEN searchBooks THEN query is updated`() = runTest {
        val bookId = "bookId"
        givenSearchBooksSuccess(bookId)
        val expectedBooks = listOf(
            getDefaultGoogleBook(bookId).toDomain(),
            Book(""),
        )
        val query = "text to search"

        viewModel.state.test {
            Assert.assertEquals(SearchUiState.Empty, awaitItem())

            viewModel.searchBooks(query = query)

            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = true,
                    query = query,
                    books = Books(),
                ),
                awaitItem(),
            )
            Assert.assertEquals(
                SearchUiState.Success(
                    isLoading = false,
                    query = query,
                    books = Books(expectedBooks),
                ),
                awaitItem(),
            )
        }
        coVerify { booksRemoteDataSource.searchBooks(query, 1, null) }
        confirmVerified(booksRemoteDataSource)
    }

    @Test
    fun `GIVEN book and no pending books WHEN addBook THEN book is added with priority 0 and success info message is shown`() =
        runTest {
            val bookId = "bookId"
            givenSearchBooksSuccess(bookId)
            viewModel.searchBooks()
            advanceUntilIdle()
            coEvery { booksLocalDataSource.insertBooks(any()) } just Runs

            viewModel.infoDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())

                viewModel.addBook(bookId)

                Assert.assertEquals(R.string.book_saved, awaitItem())
            }

            coVerify {
                booksLocalDataSource.insertBooks(
                    listOf(
                        getDefaultGoogleBook(bookId)
                            .toDomain()
                            .copy(state = BookState.PENDING, priority = 0)
                            .toLocalData(),
                    ),
                )
            }
            confirmVerified(booksLocalDataSource)
        }

    @Test
    fun `GIVEN book and N pending books WHEN addBook THEN book is added with priority N and success info message is shown`() =
        runTest {
            val savedBook = Book(id = "bookId1").copy(state = BookState.PENDING, priority = 0)
            every { booksLocalDataSource.getAllBooks() } returns flowOf(
                listOf(savedBook.toLocalData()),
            )
            viewModel.onResume()
            advanceUntilIdle()
            val bookId = "bookId2"
            givenSearchBooksSuccess(bookId)
            viewModel.searchBooks()
            advanceUntilIdle()
            coEvery { booksLocalDataSource.insertBooks(any()) } just Runs

            viewModel.infoDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())

                viewModel.addBook(bookId)

                Assert.assertEquals(R.string.book_saved, awaitItem())
            }

            coVerify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksLocalDataSource.insertBooks(
                    listOf(
                        getDefaultGoogleBook(bookId)
                            .toDomain()
                            .copy(state = BookState.PENDING, priority = 1)
                            .toLocalData(),
                    ),
                )
            }
            confirmVerified(booksLocalDataSource)
        }

    @Test
    fun `GIVEN database error WHEN addBook THEN error info message is shown`() = runTest {
        val bookId = "bookId"
        givenSearchBooksSuccess(bookId)
        viewModel.searchBooks()
        advanceUntilIdle()
        coEvery {
            booksLocalDataSource.insertBooks(any())
        } throws RuntimeException("Firestore error")

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.addBook(bookId)

            Assert.assertEquals(R.string.error_database, awaitItem())
        }

        coVerify {
            booksLocalDataSource.insertBooks(
                listOf(
                    getDefaultGoogleBook(bookId)
                        .toDomain()
                        .copy(state = BookState.PENDING, priority = 0)
                        .toLocalData(),
                ),
            )
        }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN book already saved WHEN addBook THEN error info message is shown`() = runTest {
        val bookId = "bookId"
        val book = Book(bookId)
        every { booksLocalDataSource.getAllBooks() } returns flowOf(listOf(book.toLocalData()))
        viewModel.onResume()
        advanceUntilIdle()

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.addBook(bookId)

            Assert.assertEquals(R.string.error_resource_found, awaitItem())
        }

        coVerify { booksLocalDataSource.getAllBooks() }
        coVerify(exactly = 0) { booksLocalDataSource.insertBooks(any()) }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        val bookId = "bookId"
        givenSearchBooksSuccess(bookId)
        coEvery {
            booksLocalDataSource.insertBooks(
                any(),
            )
        } throws RuntimeException("Firestore error")
        viewModel.searchBooks()
        advanceUntilIdle()

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())
            viewModel.addBook(bookId)
            Assert.assertEquals(R.string.error_database, awaitItem())

            viewModel.closeDialogs()

            Assert.assertEquals(-1, awaitItem())
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.closeDialogs()

            expectNoEvents()
        }
    }

    private fun getDefaultGoogleBook(bookId: String) = GoogleBookResponse(
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

    private fun givenSearchBooksSuccess(bookId: String) {
        val response = GoogleBookListResponse(
            totalItems = 1,
            items = listOf(getDefaultGoogleBook(bookId)),
        )
        coEvery {
            booksRemoteDataSource.searchBooks(any(), any(), any())
        } returns Result.success(response)
    }
}