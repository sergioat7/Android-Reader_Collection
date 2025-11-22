/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.books.BooksUiState
import aragones.sergio.readercollection.presentation.books.BooksViewModel
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.util.Date
import kotlin.collections.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class BooksViewModelTest {

    private val booksFlow = MutableSharedFlow<List<Book>>(replay = Int.MAX_VALUE)
    private val testSortParam = null
    private val testIsSortDescending = false
    private val booksLocalDataSource: BooksLocalDataSource = mockk {
        every { getAllBooks() } returns booksFlow.map { it.map { book -> book.toLocalData() } }
    }
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { sortParam } returns testSortParam
        every { isSortDescending } returns testIsSortDescending
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = BooksViewModel(
        BooksRepository(
            booksLocalDataSource,
            booksRemoteDataSource,
            ioDispatcher,
        ),
        UserRepository(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN books WHEN fetchBooks THEN return Success state with books sorted`() = runTest {
        val book1 = Book("1")
        val book2 = Book("2")
        val book3 = Book("3")
        booksFlow.emit(listOf(book2, book3, book1))

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()

            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN no books WHEN fetchBooks THEN return Empty state`() = runTest {
        booksFlow.emit(emptyList())

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()

            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Empty(
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN book addition WHEN fetchBooks THEN return Success state with book added`() =
        runTest {
            booksFlow.emit(emptyList())

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()

                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Empty(
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                booksFlow.emit(listOf(Book("")))

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(Book("")),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN books and new sort param emission WHEN fetchBooks THEN return Success state with books sorted by new param`() =
        runTest {
            val book1 = Book(id = "1").copy(rating = 7.0)
            val book2 = Book(id = "2").copy(rating = 5.0)
            val book3 = Book(id = "3").copy(rating = 9.0)
            booksFlow.emit(listOf(book2, book3, book1))

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()

                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.updatePickerState("rating", testIsSortDescending)

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book2, book1, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog message is reset`() = runTest {
        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())
            coEvery {
                booksLocalDataSource.updateBooks(any())
            } throws Exception()
            viewModel.setBook(Book(""))
            Assert.assertEquals(
                ErrorResponse(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
                ),
                awaitItem(),
            )

            viewModel.closeDialogs()

            Assert.assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.closeDialogs()

            expectNoEvents()
        }
    }

    @Test
    fun `WHEN showSortingPickerState THEN update state with show true`() {
        Assert.assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )

        viewModel.showSortingPickerState()

        Assert.assertEquals(
            UiSortingPickerState(
                show = true,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
    }

    @Test
    fun `GIVEN new values WHEN updatePickerState THEN update state with new values and show false`() {
        Assert.assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
        viewModel.showSortingPickerState()
        Assert.assertEquals(
            UiSortingPickerState(
                show = true,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
        val newSortParam = "newSortParam"
        val newIsSortDescending = true

        viewModel.updatePickerState(newSortParam, newIsSortDescending)

        Assert.assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = newSortParam,
                isSortDescending = newIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
    }

    @Test
    fun `GIVEN no query WHEN searchBooks THEN return Success state with all books`() = runTest {
        val book1 = Book("1")
        val book2 = Book("2")
        val book3 = Book("3")
        booksFlow.emit(listOf(book2, book3, book1))

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            viewModel.searchBooks("")

            // As new state is the same, no event is emitted
            expectNoEvents()
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN query by title or author WHEN searchBooks THEN return Success state with filtered books`() =
        runTest {
            val query = "Text to search"
            val book1 = Book("1").copy(title = "$query and more text")
            val book2 = Book("2").copy(authors = listOf(query, "author"))
            val book3 = Book("3").copy(title = "Different title")
            booksFlow.emit(listOf(book2, book3, book1))

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.searchBooks(query)

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = query,
                        isLoading = false,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2),
                        query = query,
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN query for no books WHEN searchBooks THEN return Success state with no books`() =
        runTest {
            val query = "Text to search"
            val book1 = Book("1").copy(title = "Text")
            val book2 = Book("2").copy(authors = listOf("author"))
            val book3 = Book("3").copy(title = "Different title")
            booksFlow.emit(listOf(book2, book3, book1))

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.searchBooks(query)

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = query,
                        isLoading = false,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Empty(
                        query = query,
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN query and no books WHEN searchBooks THEN return Empty state`() = runTest {
        val query = "Text to search"
        booksFlow.emit(emptyList())

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Empty(
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            viewModel.searchBooks(query)

            Assert.assertEquals(
                BooksUiState.Empty(
                    query = query,
                    isLoading = false,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN 2 pending book indexes WHEN switchBooksPriority THEN update books with new index`() =
        runTest {
            val book1 = Book("1").copy(state = BookState.PENDING, priority = 0)
            val book2 = Book("2").copy(state = BookState.PENDING, priority = 1)
            val book3 = Book("3").copy(state = BookState.PENDING, priority = 2)
            booksFlow.emit(listOf(book2, book3, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.switchBooksPriority(fromIndex = 0, toIndex = 1)
                booksFlow.emit(
                    listOf(
                        book1.copy(priority = 1),
                        book2.copy(priority = 0),
                        book3,
                    ).map {
                        it.toLocalData().toDomain()
                    },
                )

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1.copy(priority = 1), book2.copy(priority = 0), book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksLocalDataSource.updateBooks(
                    listOf(
                        book1.copy(priority = 1).toLocalData(),
                        book2.copy(priority = 0).toLocalData(),
                        book3.toLocalData(),
                    ),
                )
            }
        }

    @Test
    fun `GIVEN 2 pending book indexes out of list WHEN switchBooksPriority THEN do nothing`() =
        runTest {
            val book1 = Book("1").copy(state = BookState.PENDING, priority = 0)
            val book2 = Book("2").copy(state = BookState.PENDING, priority = 1)
            booksFlow.emit(listOf(book2, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.switchBooksPriority(fromIndex = 2, toIndex = 3)

                expectNoEvents()
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify(exactly = 0) {
                booksLocalDataSource.updateBooks(
                    listOf(
                        book1.toLocalData(),
                        book2.toLocalData(),
                    ),
                )
            }
        }

    @Test
    fun `GIVEN no pending book indexes WHEN switchBooksPriority THEN do nothing`() = runTest {
        val book1 = Book("1").copy(state = BookState.READING, priority = 0)
        val book2 = Book("2").copy(state = BookState.READ, priority = 1)
        booksFlow.emit(listOf(book2, book1))
        coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            viewModel.switchBooksPriority(fromIndex = 2, toIndex = 3)

            expectNoEvents()
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify(exactly = 0) {
            booksLocalDataSource.updateBooks(
                listOf(
                    book1.toLocalData(),
                    book2.toLocalData(),
                ),
            )
        }
    }

    @Test
    fun `GIVEN failure response WHEN switchBooksPriority THEN show error`() = runTest {
        val book1 = Book("1").copy(state = BookState.PENDING, priority = 0)
        val book2 = Book("2").copy(state = BookState.PENDING, priority = 1)
        val book3 = Book("3").copy(state = BookState.PENDING, priority = 2)
        booksFlow.emit(listOf(book2, book3, book1))
        coEvery { booksLocalDataSource.updateBooks(any()) } throws RuntimeException()

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            val state = this
            viewModel.booksError.test {
                Assert.assertEquals(null, awaitItem())

                viewModel.switchBooksPriority(fromIndex = 0, toIndex = 1)

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    state.awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    state.awaitItem(),
                )
                Assert.assertEquals(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_database,
                    ),
                    awaitItem(),
                )
            }
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify {
            booksLocalDataSource.updateBooks(
                listOf(
                    book1.copy(priority = 1).toLocalData(),
                    book2.copy(priority = 0).toLocalData(),
                    book3.toLocalData(),
                ),
            )
        }
    }

    @Test
    fun `GIVEN book with all data WHEN setBook THEN update successfully`() = runTest {
        val book1 = Book("1").copy(state = BookState.READ)
        val book2 = Book("2").copy(state = BookState.READ)
        val book3 = Book("3").copy(state = BookState.PENDING, priority = 0)
        val modifiedBook = book3.copy(state = BookState.READING)
        booksFlow.emit(listOf(book2, book3, book1))
        coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            viewModel.setBook(modifiedBook)
            booksFlow.emit(listOf(book1, book2, modifiedBook).map { it.toLocalData().toDomain() })

            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = true,
                ),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, modifiedBook),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksLocalDataSource.updateBooks(listOf(modifiedBook.toLocalData())) }
    }

    @Test
    fun `GIVEN read book without reading date WHEN setBook THEN update successfully with current date as reading date`() =
        runTest {
            val book1 = Book("1").copy(state = BookState.READ)
            val book2 = Book("2").copy(state = BookState.READ)
            val book3 = Book("3").copy(state = BookState.READING)
            val modifiedBook = book3.copy(state = BookState.READ)
            val modifiedReadBook = modifiedBook.copy(
                readingDate = Date().toString(format = null).toDate(),
            )
            booksFlow.emit(listOf(book2, book3, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.setBook(modifiedBook)
                booksFlow.emit(
                    listOf(book1, book2, modifiedReadBook).map { it.toLocalData().toDomain() },
                )

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, modifiedReadBook),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.updateBooks(listOf(modifiedReadBook.toLocalData())) }
        }

    @Test
    fun `GIVEN pending book and no pending books WHEN setBook THEN update successfully with new priority as 0`() =
        runTest {
            val book1 = Book("1").copy(state = BookState.READ, priority = 0)
            val book2 = Book("2").copy(state = BookState.READ, priority = 1)
            val book3 = Book("3").copy(state = BookState.READING)
            val modifiedBook = book3.copy(state = BookState.PENDING)
            val modifiedPendingBook = modifiedBook.copy(priority = 0)
            booksFlow.emit(listOf(book2, book3, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.setBook(modifiedBook)
                booksFlow.emit(
                    listOf(book1, book2, modifiedPendingBook).map { it.toLocalData().toDomain() },
                )

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, modifiedPendingBook),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.updateBooks(listOf(modifiedPendingBook.toLocalData())) }
        }

    @Test
    fun `GIVEN pending book and pending books WHEN setBook THEN update successfully with new priority as max priority + 1`() =
        runTest {
            val book1 = Book("1").copy(state = BookState.PENDING, priority = 0)
            val book2 = Book("2").copy(state = BookState.READ, priority = 1)
            val book3 = Book("3").copy(state = BookState.READING)
            val modifiedBook = book3.copy(state = BookState.PENDING)
            val modifiedPendingBook = modifiedBook.copy(priority = 1)
            booksFlow.emit(listOf(book2, book3, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = false),
                    awaitItem(),
                )

                viewModel.fetchBooks()
                Assert.assertEquals(
                    BooksUiState.Empty(query = "", isLoading = true),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )

                viewModel.setBook(modifiedBook)
                booksFlow.emit(
                    listOf(book1, book2, modifiedPendingBook).map { it.toLocalData().toDomain() },
                )

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, modifiedPendingBook),
                        query = "",
                        isLoading = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.updateBooks(listOf(modifiedPendingBook.toLocalData())) }
        }

    @Test
    fun `GIVEN failure response WHEN setBook THEN show error`() = runTest {
        val book1 = Book("1").copy(state = BookState.READ)
        val book2 = Book("2").copy(state = BookState.READ)
        val book3 = Book("3").copy(state = BookState.PENDING, priority = 0)
        val modifiedBook = book3.copy(state = BookState.READING)
        booksFlow.emit(listOf(book2, book3, book1))
        coEvery { booksLocalDataSource.updateBooks(any()) } throws RuntimeException()

        viewModel.state.test {
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = false),
                awaitItem(),
            )

            viewModel.fetchBooks()
            Assert.assertEquals(
                BooksUiState.Empty(query = "", isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                BooksUiState.Success(
                    books = listOf(book1, book2, book3),
                    query = "",
                    isLoading = false,
                ),
                awaitItem(),
            )

            val state = this
            viewModel.booksError.test {
                Assert.assertEquals(null, awaitItem())

                viewModel.setBook(modifiedBook)

                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = true,
                    ),
                    state.awaitItem(),
                )
                Assert.assertEquals(
                    BooksUiState.Success(
                        books = listOf(book1, book2, book3),
                        query = "",
                        isLoading = false,
                    ),
                    state.awaitItem(),
                )
                Assert.assertEquals(
                    ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_server,
                    ),
                    awaitItem(),
                )
            }
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksLocalDataSource.updateBooks(listOf(modifiedBook.toLocalData())) }
    }
}