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
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.GoogleVolumeResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.bookdetail.BookDetailUiState
import aragones.sergio.readercollection.presentation.bookdetail.BookDetailViewModel
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BookDetailViewModelTest {

    private val testBookId = "bookId"
    private val savedStateHandle: SavedStateHandle = SavedStateHandle().apply {
        this["bookId"] = testBookId
    }
    private val booksFlow = MutableSharedFlow<List<Book>>(replay = Int.MAX_VALUE)
    private val booksLocalDataSource: BooksLocalDataSource = mockk {
        every { getAllBooks() } returns booksFlow.map { it.map { book -> book.toLocalData() } }
    }
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = BookDetailViewModel(
        savedStateHandle,
        BooksRepository(
            booksLocalDataSource,
            booksRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN friend id and success response WHEN onCreate THEN update state with friend book and isAlreadySaved false and isEditable true`() =
        runTest {
            val testFriendId = "friendId"
            val savedStateHandle = SavedStateHandle().apply {
                this["bookId"] = testBookId
                this["friendId"] = testFriendId
            }
            val viewModel = BookDetailViewModel(
                savedStateHandle,
                BooksRepository(
                    booksLocalDataSource,
                    booksRemoteDataSource,
                    ioDispatcher,
                ),
            )
            val book = Book(testBookId)
            coEvery {
                booksRemoteDataSource.getFriendBook(any(), any())
            } returns Result.success(book.toRemoteData())

            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )

                viewModel.onCreate()

                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isEditable = true,
                        isAlreadySaved = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksRemoteDataSource.getFriendBook(testFriendId, testBookId) }
        }

    @Test
    fun `GIVEN friend id and failure response WHEN onCreate THEN show error`() = runTest {
        val testFriendId = "friendId"
        val savedStateHandle = SavedStateHandle().apply {
            this["bookId"] = testBookId
            this["friendId"] = testFriendId
        }
        val viewModel = BookDetailViewModel(
            savedStateHandle,
            BooksRepository(
                booksLocalDataSource,
                booksRemoteDataSource,
                ioDispatcher,
            ),
        )
        coEvery {
            booksRemoteDataSource.getFriendBook(any(), any())
        } returns Result.failure(RuntimeException("Firestore error"))

        viewModel.bookDetailError.test {
            assertEquals(null, awaitItem())

            viewModel.onCreate()

            assertEquals(
                ErrorModel("", R.string.error_no_book),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksRemoteDataSource.getFriendBook(testFriendId, testBookId) }
    }

    @Test
    fun `GIVEN no friend id and saved book WHEN onCreate THEN update state with book and isAlreadySaved true and isEditable false`() =
        runTest {
            val book = Book(testBookId)
            coEvery { booksLocalDataSource.getBook(any()) } returns book.toLocalData()

            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )

                viewModel.onCreate()

                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.getBook(testBookId) }
        }

    @Test
    fun `GIVEN no friend id and no saved book and success response WHEN onCreate THEN update state with book and isAlreadySaved false and isEditable true`() =
        runTest {
            val book = GoogleBookResponse(
                id = testBookId,
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
            coEvery { booksLocalDataSource.getBook(any()) } returns null
            coEvery {
                booksRemoteDataSource.getBook(any())
            } returns Result.success(book)

            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )

                viewModel.onCreate()

                assertEquals(
                    BookDetailUiState(
                        book = book.toDomain(),
                        isAlreadySaved = false,
                        isEditable = true,
                    ),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify { booksRemoteDataSource.getBook(testBookId) }
        }

    @Test
    fun `GIVEN no friend id and no saved book and failure response WHEN onCreate THEN show error`() =
        runTest {
            coEvery { booksLocalDataSource.getBook(any()) } returns null
            coEvery {
                booksRemoteDataSource.getBook(any())
            } returns Result.failure(RuntimeException("Firestore error"))

            viewModel.bookDetailError.test {
                assertEquals(null, awaitItem())

                viewModel.onCreate()

                assertEquals(
                    ErrorModel("", R.string.error_no_book),
                    awaitItem(),
                )
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify { booksRemoteDataSource.getBook(testBookId) }
        }

    @Test
    fun `GIVEN no friend id and database error WHEN onCreate THEN show error`() = runTest {
        coEvery { booksLocalDataSource.getBook(any()) } throws RuntimeException("Database error")

        viewModel.bookDetailError.test {
            assertEquals(null, awaitItem())

            viewModel.onCreate()

            assertEquals(
                ErrorModel("", R.string.error_no_book),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksLocalDataSource.getBook(testBookId) }
    }

    @Test
    fun `WHEN enableEdition THEN update state with editable true`() = runTest {
        viewModel.state.test {
            assertEquals(
                BookDetailUiState(
                    book = null,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )

            viewModel.enableEdition()

            assertEquals(
                BookDetailUiState(
                    book = null,
                    isAlreadySaved = true,
                    isEditable = true,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `WHEN disableEdition THEN update state with editable false and unmodified book data`() =
        runTest {
            val book = Book(testBookId)
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs
            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
                viewModel.setBook(book)
                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
                viewModel.enableEdition()
                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = true,
                    ),
                    awaitItem(),
                )

                viewModel.disableEdition()

                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksLocalDataSource.updateBooks(listOf(book.toLocalData())) }
        }

    @Test
    fun `GIVEN book WHEN changeData THEN updates state with book`() = runTest {
        val book = Book("bookId")
        viewModel.state.test {
            assertEquals(
                BookDetailUiState(
                    book = null,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )

            viewModel.changeData(book)

            assertEquals(
                BookDetailUiState(
                    book = book,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun `GIVEN book and pending books and success response WHEN createBook THEN show success message and update state with isAlreadySaved true and isEditable false`() =
        runTest {
            val book = GoogleBookResponse(
                id = testBookId,
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
            val newBook = book.toDomain().copy(priority = 2)
            coEvery { booksLocalDataSource.getBook(any()) } returns null
            coEvery {
                booksRemoteDataSource.getBook(any())
            } returns Result.success(book)
            coEvery { booksLocalDataSource.insertBooks(any()) } just Runs
            booksFlow.emit(
                listOf(
                    Book("1").copy(state = BookState.PENDING, priority = 0),
                    Book("2").copy(state = BookState.PENDING, priority = 1),
                ),
            )

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())

                viewModel.state.test {
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )
                    viewModel.onCreate()
                    assertEquals(
                        BookDetailUiState(
                            book = book.toDomain(),
                            isAlreadySaved = false,
                            isEditable = true,
                        ),
                        awaitItem(),
                    )

                    viewModel.createBook(book.toDomain())

                    assertEquals(
                        R.string.book_saved,
                        infoDialogMessage.awaitItem(),
                    )
                    assertEquals(
                        BookDetailUiState(
                            book = newBook,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )
                }
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify { booksRemoteDataSource.getBook(testBookId) }
            coVerify { booksLocalDataSource.insertBooks(listOf(newBook.toLocalData())) }
        }

    @Test
    fun `GIVEN book and no pending books and success response WHEN createBook THEN show success message and update state with isAlreadySaved true and isEditable false`() =
        runTest {
            val book = GoogleBookResponse(
                id = testBookId,
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
            val newBook = book.toDomain().copy(priority = 0)
            coEvery { booksLocalDataSource.getBook(any()) } returns null
            coEvery {
                booksRemoteDataSource.getBook(any())
            } returns Result.success(book)
            coEvery { booksLocalDataSource.insertBooks(any()) } just Runs
            booksFlow.emit(emptyList())

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())

                viewModel.state.test {
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )
                    viewModel.onCreate()
                    assertEquals(
                        BookDetailUiState(
                            book = book.toDomain(),
                            isAlreadySaved = false,
                            isEditable = true,
                        ),
                        awaitItem(),
                    )

                    viewModel.createBook(book.toDomain())

                    assertEquals(
                        R.string.book_saved,
                        infoDialogMessage.awaitItem(),
                    )
                    assertEquals(
                        BookDetailUiState(
                            book = newBook,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )
                }
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify { booksRemoteDataSource.getBook(testBookId) }
            coVerify { booksLocalDataSource.insertBooks(listOf(newBook.toLocalData())) }
        }

    @Test
    fun `GIVEN failure response WHEN createBook THEN show error`() = runTest {
        val book = GoogleBookResponse(
            id = testBookId,
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
        val newBook = book.toDomain().copy(priority = 0)
        coEvery { booksLocalDataSource.getBook(any()) } returns null
        coEvery {
            booksRemoteDataSource.getBook(any())
        } returns Result.success(book)
        coEvery {
            booksLocalDataSource.insertBooks(
                any(),
            )
        } throws RuntimeException("Firestore error")
        booksFlow.emit(emptyList())
        viewModel.onCreate()

        viewModel.bookDetailError.test {
            assertEquals(null, awaitItem())

            viewModel.createBook(book.toDomain())

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                ),
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksLocalDataSource.getBook(testBookId) }
        coVerify { booksRemoteDataSource.getBook(testBookId) }
        coVerify { booksLocalDataSource.insertBooks(listOf(newBook.toLocalData())) }
    }

    @Test
    fun `GIVEN book and success response WHEN setBook THEN update state with updated book data`() =
        runTest {
            val book = Book(testBookId)
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs
            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )

                viewModel.setBook(book)

                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksLocalDataSource.updateBooks(listOf(book.toLocalData())) }
        }

    @Test
    fun `GIVEN failure response WHEN setBook THEN show error`() = runTest {
        val book = Book(testBookId)
        coEvery {
            booksLocalDataSource.updateBooks(any())
        } throws RuntimeException("Database error")
        viewModel.bookDetailError.test {
            assertEquals(null, awaitItem())

            viewModel.setBook(book)

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                ),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource.updateBooks(listOf(book.toLocalData())) }
    }

    @Test
    fun `GIVEN existent book and success response WHEN deleteBook THEN show success message and update state with isAlreadySaved false and isEditable true`() =
        runTest {
            val deletedBook = Book(testBookId)
            coEvery {
                booksLocalDataSource.getBook(any())
            } returns deletedBook.toLocalData()
            coEvery {
                booksLocalDataSource.deleteBooks(any())
            } just Runs
            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())

                viewModel.state.test {
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )

                    viewModel.deleteBook()

                    assertEquals(
                        R.string.book_removed,
                        infoDialogMessage.awaitItem(),
                    )
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = false,
                            isEditable = true,
                        ),
                        awaitItem(),
                    )
                }
            }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify { booksLocalDataSource.deleteBooks(listOf(deletedBook.toLocalData())) }
        }

    @Test
    fun `GIVEN non existent book and success response WHEN deleteBook THEN show success message and update state with isAlreadySaved false and isEditable true`() =
        runTest {
            coEvery {
                booksLocalDataSource.getBook(any())
            } returns null
            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())

                viewModel.state.test {
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = true,
                            isEditable = false,
                        ),
                        awaitItem(),
                    )

                    viewModel.deleteBook()

                    assertEquals(
                        R.string.book_removed,
                        infoDialogMessage.awaitItem(),
                    )
                    assertEquals(
                        BookDetailUiState(
                            book = null,
                            isAlreadySaved = false,
                            isEditable = true,
                        ),
                        awaitItem(),
                    )
                }
            }
            coVerify { booksLocalDataSource.getBook(testBookId) }
            coVerify(exactly = 0) { booksLocalDataSource.deleteBooks(any()) }
        }

    @Test
    fun `GIVEN failure response WHEN deleteBook THEN show error`() = runTest {
        coEvery {
            booksLocalDataSource.getBook(any())
        } throws RuntimeException("Database error")
        viewModel.bookDetailError.test {
            assertEquals(null, awaitItem())

            viewModel.deleteBook()

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                ),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource.getBook(testBookId) }
        coVerify(exactly = 0) { booksLocalDataSource.deleteBooks(any()) }
    }

    @Test
    fun `GIVEN valid uri WHEN setBookImage THEN updates book data in state with uri`() = runTest {
        val book = Book(testBookId)
        val imageUri = "uri"
        coEvery { booksLocalDataSource.updateBooks(any()) } just Runs
        viewModel.state.test {
            assertEquals(
                BookDetailUiState(
                    book = null,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )
            viewModel.setBook(book)
            assertEquals(
                BookDetailUiState(
                    book = book,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )

            viewModel.setBookImage(imageUri)

            assertEquals(
                BookDetailUiState(
                    book = book.copy(thumbnail = imageUri),
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource.updateBooks(listOf(book.toLocalData())) }
    }

    @Test
    fun `GIVEN null uri WHEN setBookImage THEN updates book data in state with a null uri`() =
        runTest {
            val book = Book(testBookId).copy(thumbnail = "uri")
            coEvery { booksLocalDataSource.updateBooks(any()) } just Runs
            viewModel.state.test {
                assertEquals(
                    BookDetailUiState(
                        book = null,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
                viewModel.setBook(book)
                assertEquals(
                    BookDetailUiState(
                        book = book,
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )

                viewModel.setBookImage(null)

                assertEquals(
                    BookDetailUiState(
                        book = book.copy(thumbnail = null),
                        isAlreadySaved = true,
                        isEditable = false,
                    ),
                    awaitItem(),
                )
            }
            coVerify { booksLocalDataSource.updateBooks(listOf(book.toLocalData())) }
        }

    @Test
    fun `GIVEN no book saved in state WHEN setBookImage THEN do nothing`() = runTest {
        viewModel.state.test {
            assertEquals(
                BookDetailUiState(
                    book = null,
                    isAlreadySaved = true,
                    isEditable = false,
                ),
                awaitItem(),
            )

            viewModel.setBookImage("uri")

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.book_remove_confirmation)

            assertEquals(R.string.book_remove_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.book_remove_confirmation)
            assertEquals(R.string.book_remove_confirmation, awaitItem())

            viewModel.showConfirmationDialog(R.string.book_remove_confirmation)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN showImageDialog THEN dialog is shown`() = runTest {
        viewModel.imageDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.showImageDialog(R.string.enter_valid_url)

            assertEquals(R.string.enter_valid_url, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showImageDialog THEN do nothing`() = runTest {
        viewModel.imageDialogMessageId.test {
            assertEquals(-1, awaitItem())
            viewModel.showImageDialog(R.string.enter_valid_url)
            assertEquals(R.string.enter_valid_url, awaitItem())

            viewModel.showImageDialog(R.string.enter_valid_url)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            val confirmationDialogMessage = this
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.book_remove_confirmation)
            assertEquals(
                R.string.book_remove_confirmation,
                confirmationDialogMessage.awaitItem(),
            )

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())
                coEvery {
                    booksLocalDataSource.getBook(any())
                } returns null
                viewModel.deleteBook()
                assertEquals(
                    R.string.book_removed,
                    infoDialogMessage.awaitItem(),
                )

                viewModel.imageDialogMessageId.test {
                    val imageDialogMessage = this
                    assertEquals(-1, awaitItem())
                    viewModel.showImageDialog(R.string.enter_valid_url)
                    assertEquals(R.string.enter_valid_url, awaitItem())

                    viewModel.closeDialogs()

                    assertEquals(-1, confirmationDialogMessage.awaitItem())
                    assertEquals(-1, infoDialogMessage.awaitItem())
                    assertEquals(-1, imageDialogMessage.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.infoDialogMessageId.test {
                assertEquals(-1, awaitItem())

                viewModel.imageDialogMessageId.test {
                    assertEquals(-1, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }
}