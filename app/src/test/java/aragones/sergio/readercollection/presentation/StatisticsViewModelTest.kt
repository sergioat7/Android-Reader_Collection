/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.statistics.BarEntries
import aragones.sergio.readercollection.presentation.statistics.MapEntries
import aragones.sergio.readercollection.presentation.statistics.PieEntries
import aragones.sergio.readercollection.presentation.statistics.StatisticsUiState
import aragones.sergio.readercollection.presentation.statistics.StatisticsViewModel
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.extensions.toDate
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import io.mockk.Called
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class StatisticsViewModelTest {

    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { sortParam } returns "sortParam"
        every { isSortDescending } returns true
        every { language } returns "en"
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = StatisticsViewModel(
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
    fun `GIVEN read books WHEN fetch books THEN return Success state with statistics data`() =
        runTest {
            val book1 = Book(id = "bookId1").copy(
                authors = listOf("Author 1"),
                readingDate = "2025-10-05".toDate(),
                pageCount = 100,
                format = "PHYSICAL",
            )
            val book2 = Book(id = "bookId2").copy(
                authors = listOf("Author 2"),
                readingDate = "2025-09-05".toDate(),
                pageCount = 10,
            )
            val books = listOf(book1, book2)
            every { booksLocalDataSource.getReadBooks() } returns flowOf(
                books.map { it.toLocalData() },
            )

            viewModel.state.test {
                Assert.assertEquals(StatisticsUiState.Empty, awaitItem())

                viewModel.fetchBooks()

                Assert.assertEquals(
                    StatisticsUiState.Success.empty().copy(isLoading = true),
                    awaitItem(),
                )
                val expected = StatisticsUiState.Success(
                    totalBooksRead = books.size,
                    booksByYearEntries = BarEntries(listOf(BarEntry(2025f, 2f))),
                    booksByMonthEntries = PieEntries(
                        listOf(
                            PieEntry(1f, "Sep"),
                            PieEntry(1f, "Oct"),
                        ),
                    ),
                    booksByAuthorStats = MapEntries(
                        mapOf(
                            "Author 1" to listOf(book1),
                            "Author 2" to listOf(book2),
                        ),
                    ),
                    shorterBook = book2,
                    longerBook = book1,
                    booksByFormatEntries = PieEntries(listOf(PieEntry(1f, "Physical"))),
                    isLoading = false,
                )
                val result = awaitItem()
                Assert.assertTrue(result is StatisticsUiState.Success)
                result as StatisticsUiState.Success
                Assert.assertEquals(
                    expected.totalBooksRead,
                    result.totalBooksRead,
                )
                Assert.assertEquals(
                    expected.booksByYearEntries.entries.map { it.x to it.y },
                    result.booksByYearEntries.entries.map { it.x to it.y },
                )
                Assert.assertEquals(
                    expected.booksByMonthEntries.entries.map { it.value to it.label },
                    result.booksByMonthEntries.entries.map { it.value to it.label },
                )
                Assert.assertEquals(
                    expected.booksByAuthorStats,
                    result.booksByAuthorStats,
                )
                Assert.assertEquals(
                    expected.shorterBook,
                    result.shorterBook,
                )
                Assert.assertEquals(
                    expected.longerBook,
                    result.longerBook,
                )
                Assert.assertEquals(
                    expected.booksByFormatEntries.entries.map { it.value to it.label },
                    result.booksByFormatEntries.entries.map { it.value to it.label },
                )
                Assert.assertEquals(
                    expected.isLoading,
                    result.isLoading,
                )
            }
            verify { booksLocalDataSource.getReadBooks() }
            confirmVerified(booksLocalDataSource)
        }

    @Test
    fun `GIVEN no read books WHEN fetch books THEN return Empty state`() = runTest {
        every { booksLocalDataSource.getReadBooks() } returns flowOf(emptyList())

        viewModel.state.test {
            Assert.assertEquals(StatisticsUiState.Empty, awaitItem())

            viewModel.fetchBooks()

            Assert.assertEquals(
                StatisticsUiState.Success.empty().copy(isLoading = true),
                awaitItem(),
            )
            Assert.assertEquals(
                StatisticsUiState.Empty,
                awaitItem(),
            )
        }
        verify { booksLocalDataSource.getReadBooks() }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.export_confirmation)

            Assert.assertEquals(R.string.export_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN dialog message is not updated`() =
        runTest {
            viewModel.confirmationDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())
                viewModel.showConfirmationDialog(R.string.export_confirmation)
                Assert.assertEquals(R.string.export_confirmation, awaitItem())

                viewModel.showConfirmationDialog(R.string.export_confirmation)

                expectNoEvents()
            }
        }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        coEvery { booksLocalDataSource.importDataFrom(any()) } throws Exception()
        viewModel.booksError.test {
            val booksError = this
            Assert.assertEquals(null, awaitItem())
            viewModel.importData("[]")
            Assert.assertEquals(
                ErrorModel("", R.string.error_file_data),
                awaitItem(),
            )

            viewModel.confirmationDialogMessageId.test {
                val confirmationDialogMessage = this
                Assert.assertEquals(-1, awaitItem())
                viewModel.showConfirmationDialog(R.string.export_confirmation)
                Assert.assertEquals(
                    R.string.export_confirmation,
                    confirmationDialogMessage.awaitItem(),
                )

                viewModel.infoDialogMessageId.test {
                    val infoDialogMessage = this
                    Assert.assertEquals(-1, awaitItem())

                    coEvery { booksLocalDataSource.importDataFrom(any()) } just Runs
                    viewModel.importData("[]")
                    Assert.assertEquals(
                        R.string.data_imported,
                        infoDialogMessage.awaitItem(),
                    )

                    viewModel.closeDialogs()

                    Assert.assertEquals(null, booksError.awaitItem())
                    Assert.assertEquals(-1, confirmationDialogMessage.awaitItem())
                    Assert.assertEquals(-1, infoDialogMessage.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.confirmationDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())

                viewModel.infoDialogMessageId.test {
                    Assert.assertEquals(-1, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }

    @Test
    fun `GIVEN valid json data WHEN importData THEN data is saved`() = runTest {
        val book = Book("bookId")
        coEvery { booksLocalDataSource.importDataFrom(any()) } just Runs

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.importData(
                """
                [{
                    "googleId": "${book.id}",
                    "pageCount": ${book.pageCount},
                    "averageRating": ${book.averageRating},
                    "ratingsCount": ${book.ratingsCount},
                    "rating": ${book.rating},
                    "priority": ${book.priority}
                }]
                """.trimIndent(),
            )

            Assert.assertEquals(R.string.data_imported, awaitItem())
        }
        coVerify { booksLocalDataSource.importDataFrom(listOf(book.toLocalData())) }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN invalid json data WHEN importData THEN data is saved`() = runTest {
        coEvery { booksLocalDataSource.importDataFrom(any()) } just Runs

        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.importData("[{}]")

            Assert.assertEquals(
                ErrorModel("", R.string.error_file_data),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource wasNot Called }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN error on save data WHEN importData THEN error is shown`() = runTest {
        val book = Book("bookId")
        coEvery { booksLocalDataSource.importDataFrom(any()) } throws Exception()

        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.importData(
                """
                [{
                    "googleId": "${book.id}",
                    "pageCount": ${book.pageCount},
                    "averageRating": ${book.averageRating},
                    "ratingsCount": ${book.ratingsCount},
                    "rating": ${book.rating},
                    "priority": ${book.priority}
                }]
                """.trimIndent(),
            )

            Assert.assertEquals(
                ErrorModel("", R.string.error_file_data),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource.importDataFrom(listOf(book.toLocalData())) }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN books WHEN getDataToExport THEN returns json with books data`() = runTest {
        var json: String? = null
        val book = Book(id = "bookId")
        every { booksLocalDataSource.getAllBooks() } returns flowOf(listOf(book.toLocalData()))

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            Assert.assertEquals(R.string.file_created, awaitItem())
            Assert.assertEquals(
                """
                [{
                    "googleId":"${book.id}",
                    "pageCount":${book.pageCount},
                    "averageRating":${book.averageRating},
                    "ratingsCount":${book.ratingsCount},
                    "rating":${book.rating},
                    "priority":${book.priority}
                }]
                """.trimIndent().replace("\n", "").replace(" ", ""),
                json,
            )
        }
        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN no books WHEN getDataToExport THEN returns empty json`() = runTest {
        var json: String? = null
        every { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())

        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            Assert.assertEquals(R.string.file_created, awaitItem())
            Assert.assertEquals("[]", json)
        }
        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN failure on getting books WHEN getDataToExport THEN error is shown`() = runTest {
        var json: String? = null
        every { booksLocalDataSource.getAllBooks() } throws Exception()

        viewModel.booksError.test {
            Assert.assertEquals(null, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            Assert.assertEquals(
                ErrorModel("", R.string.error_database),
                awaitItem(),
            )
            Assert.assertEquals(null, json)
        }
        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }
}