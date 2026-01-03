/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/11/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.BooksRepositoryImpl
import aragones.sergio.readercollection.data.UserRepositoryImpl
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.booklist.BookListUiState
import aragones.sergio.readercollection.presentation.booklist.BookListViewModel
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import aragones.sergio.readercollection.presentation.utils.MainDispatcherRule
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BookListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testUserId = "userId"
    private val testState = ""
    private val booksFlow = MutableSharedFlow<List<Book>>(replay = Int.MAX_VALUE)
    private val testSortParam = null
    private val testIsSortDescending = false
    private val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        this["state"] = testState
        this["sortParam"] = testSortParam
        this["isSortDescending"] = testIsSortDescending
        this["query"] = ""
    }
    private val booksLocalDataSource: BooksLocalDataSource = mockk {
        every { getAllBooks() } returns booksFlow.map { it.map { book -> book.toLocalData() } }
    }
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { userId } returns testUserId
        every { language } returns "en"
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val viewModel = BookListViewModel(
        savedStateHandle,
        BooksRepositoryImpl(
            booksLocalDataSource,
            booksRemoteDataSource,
            mainDispatcherRule.testDispatcher,
        ),
        UserRepositoryImpl(
            userLocalDataSource,
            userRemoteDataSource,
            mainDispatcherRule.testDispatcher,
        ),
    )

    @Test
    fun `GIVEN books WHEN fetchBooks THEN return Success state`() = runTest {
        val book1 = Book("1")
        val book2 = Book("2")
        val book3 = Book("3")
        booksFlow.emit(listOf(book2, book3, book1))

        viewModel.state.test {
            assertEquals(
                BookListUiState(
                    isLoading = true,
                    books = Books(),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )

            viewModel.fetchBooks()

            assertEquals(
                BookListUiState(
                    isLoading = false,
                    books = Books(listOf(book1, book2, book3)),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN no books WHEN fetchBooks THEN show error`() = runTest {
        booksFlow.emit(emptyList())

        viewModel.state.test {
            val state = this
            assertEquals(
                BookListUiState(
                    isLoading = true,
                    books = Books(),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )
            viewModel.booksError.test {
                assertEquals(null, awaitItem())

                viewModel.fetchBooks()

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    state.awaitItem(),
                )
                assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.error_database,
                    ),
                    awaitItem(),
                )
            }
        }
        verify { booksLocalDataSource.getAllBooks() }
    }

    @Test
    fun `GIVEN books update WHEN fetchBooks THEN return Success state with books updated`() =
        runTest {
            val book1 = Book("1")
            val book2 = Book("2")
            val book3 = Book("3")
            booksFlow.emit(listOf(book2, book3, book1))

            viewModel.state.test {
                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.fetchBooks()

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book1, book2, book3)),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                val book1Updated = book1.copy(title = "1")
                val book2Updated = book2.copy(title = "2")
                val book3Updated = book3.copy(title = "3")
                booksFlow.emit(listOf(book1Updated, book2Updated, book3Updated))

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book1Updated, book2Updated, book3Updated)),
                        subtitle = "",
                        isDraggingEnabled = false,
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
                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.fetchBooks()

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book1, book2, book3)),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.updatePickerState("rating", testIsSortDescending)

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book2, book1, book3)),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN params pass to view model WHEN fetchBooks THEN return Success state with books filtered and subtitle formed`() =
        runTest {
            val testState = BookState.READ
            val testSortParam = "rating"
            val testIsSortDescending = true
            val testQuery = "test"
            val testYear = 2025
            val testAuthor = "writer"
            val testFormat = "PHYSICAL"
            val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
                this["state"] = testState
                this["sortParam"] = testSortParam
                this["isSortDescending"] = testIsSortDescending
                this["query"] = testQuery
                this["year"] = testYear
                this["author"] = testAuthor
                this["format"] = testFormat
            }
            val viewModel = BookListViewModel(
                savedStateHandle,
                BooksRepositoryImpl(
                    booksLocalDataSource,
                    booksRemoteDataSource,
                    mainDispatcherRule.testDispatcher,
                ),
                UserRepositoryImpl(
                    userLocalDataSource,
                    userRemoteDataSource,
                    mainDispatcherRule.testDispatcher,
                ),
            )
            val book1 = Book("1").copy(
                title = testQuery,
                authors = listOf(testAuthor, "another author"),
                readingDate = LocalDate(2025, 11, 22),
                rating = 0.0,
                format = testFormat,
                state = testState,
            )
            val book2 = book1.copy(id = "2", rating = 10.0)
            val book3 = book1.copy(id = "3", rating = 5.0)
            booksFlow.emit(
                listOf(
                    book1,
                    book2,
                    book3,
                    book1.copy(id = "4", title = "title"),
                    book1.copy(id = "5", authors = listOf("another author")),
                    book1.copy(id = "6", readingDate = LocalDate(2024, 11, 22)),
                    book1.copy(id = "7", format = "DIGITAL"),
                    book1.copy(id = "8", state = BookState.PENDING),
                ),
            )

            viewModel.state.test {
                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.fetchBooks()

                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "2025,writer,Physical",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )
                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book2, book3, book1)),
                        subtitle = "2025,writer,Physical",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
        }

    @Test
    fun `GIVEN Success state with isDraggingEnabled false WHEN switchDraggingState THEN update state with isDraggingEnabled true`() =
        runTest {
            viewModel.state.test {
                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.switchDraggingState()

                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = true,
                    ),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `GIVEN books WHEN updateBookOrdering THEN update state with books`() = runTest {
        val book1 = Book("1").copy(priority = 0)
        val book2 = Book("2").copy(priority = 1)
        val book3 = Book("3").copy(priority = 2)
        viewModel.state.test {
            assertEquals(
                BookListUiState(
                    isLoading = true,
                    books = Books(),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )

            viewModel.updateBookOrdering(listOf(book2, book3, book1))

            assertEquals(
                BookListUiState(
                    isLoading = true,
                    books = Books(
                        listOf(
                            book1.copy(priority = 2),
                            book2.copy(priority = 0),
                            book3.copy(priority = 1),
                        ),
                    ),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `GIVEN success response WHEN setPriorityFor THEN update books with new priority`() =
        runTest {
            val book1 = Book("1").copy(priority = 0)
            val book2 = Book("2").copy(priority = 1)
            val book3 = Book("3").copy(priority = 2)
            val updatedBook1 = book1.copy(priority = 1)
            val updatedBook2 = book2.copy(priority = 0)
            booksFlow.emit(listOf(book2, book3, book1))
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs

            viewModel.state.test {
                assertEquals(
                    BookListUiState(
                        isLoading = true,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )
                viewModel.fetchBooks()
                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(book1, book2, book3)),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )

                viewModel.setPriorityFor(listOf(updatedBook1, updatedBook2, book3))
                booksFlow.emit(
                    listOf(updatedBook1, updatedBook2, book3)
                        .map { it.toLocalData().toDomain() },
                )

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(listOf(updatedBook1, updatedBook2, book3)),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksLocalDataSource.updateBooks(
                    listOf(
                        updatedBook1.toLocalData(),
                        updatedBook2.toLocalData(),
                        book3.toLocalData(),
                    ),
                )
            }
        }

    @Test
    fun `GIVEN failure response WHEN setPriorityFor THEN show error`() = runTest {
        val book1 = Book("1").copy(priority = 0)
        val book2 = Book("2").copy(priority = 1)
        val book3 = Book("3").copy(priority = 2)
        booksFlow.emit(listOf(book2, book3, book1))
        coEvery { booksLocalDataSource.updateBooks(any()) } throws RuntimeException()

        viewModel.state.test {
            val state = this
            assertEquals(
                BookListUiState(
                    isLoading = true,
                    books = Books(),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )
            viewModel.fetchBooks()
            assertEquals(
                BookListUiState(
                    isLoading = false,
                    books = Books(listOf(book1, book2, book3)),
                    subtitle = "",
                    isDraggingEnabled = false,
                ),
                awaitItem(),
            )

            viewModel.booksError.test {
                assertEquals(null, awaitItem())

                viewModel.setPriorityFor(listOf(book1, book2, book3))

                assertEquals(
                    BookListUiState(
                        isLoading = false,
                        books = Books(),
                        subtitle = "",
                        isDraggingEnabled = false,
                    ),
                    state.awaitItem(),
                )
                assertEquals(
                    ErrorModel(
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
                    book1.toLocalData(),
                    book2.toLocalData(),
                    book3.toLocalData(),
                ),
            )
        }
    }

    @Test
    fun `WHEN showSortingPickerState THEN update state with show true`() {
        assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )

        viewModel.showSortingPickerState()

        assertEquals(
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
        assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = testSortParam,
                isSortDescending = testIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
        viewModel.showSortingPickerState()
        assertEquals(
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

        assertEquals(
            UiSortingPickerState(
                show = false,
                sortParam = newSortParam,
                isSortDescending = newIsSortDescending,
            ),
            viewModel.sortingPickerState.value,
        )
    }
}