/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.data.BooksRepositoryImpl
import aragones.sergio.readercollection.data.UserRepositoryImpl
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.data.remote.model.FormatResponse
import aragones.sergio.readercollection.data.remote.model.GenreResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.statistics.Entries
import aragones.sergio.readercollection.presentation.statistics.Entry
import aragones.sergio.readercollection.presentation.statistics.MapEntries
import aragones.sergio.readercollection.presentation.statistics.StatisticsUiState
import aragones.sergio.readercollection.presentation.statistics.StatisticsViewModel
import aragones.sergio.readercollection.presentation.utils.MainDispatcherRule
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.extensions.toString
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlin.String
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Assert
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.data_imported
import reader_collection.app.generated.resources.error_database
import reader_collection.app.generated.resources.error_file_data
import reader_collection.app.generated.resources.export_confirmation
import reader_collection.app.generated.resources.file_created

class StatisticsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { sortParam } returns "sortParam"
        every { isSortDescending } returns true
        every { language } returns "en"
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val viewModel = StatisticsViewModel(
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
    fun `GIVEN read books WHEN fetch books THEN return Success state with statistics data`() =
        runTest {
            FORMATS = listOf(FormatResponse("PHYSICAL", "Physical"))
            val book1 = Book(id = "bookId1").copy(
                authors = listOf("Author 1"),
                readingDate = LocalDate(2025, 10, 5),
                pageCount = 100,
                format = "PHYSICAL",
            )
            val book2 = Book(id = "bookId2").copy(
                authors = listOf("Author 2"),
                readingDate = LocalDate(2025, 9, 5),
                pageCount = 10,
            )
            val books = listOf(book1, book2)
            every { booksLocalDataSource.getReadBooks() } returns flowOf(
                books.map { it.toLocalData() },
            )

            viewModel.state.test {
                assertEquals(StatisticsUiState.Empty, awaitItem())

                viewModel.fetchBooks()

                assertEquals(
                    StatisticsUiState.Success.empty().copy(isLoading = true),
                    awaitItem(),
                )
                val expected = StatisticsUiState.Success(
                    totalBooksRead = books.size,
                    booksByYearEntries = Entries(listOf(Entry("2025", 2))),
                    booksByMonthEntries = Entries(
                        listOf(
                            Entry("Sep", 1),
                            Entry("Oct", 1),
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
                    booksByFormatEntries = Entries(listOf(Entry("Physical", 1))),
                    isLoading = false,
                )
                val result = awaitItem()
                Assert.assertTrue(result is StatisticsUiState.Success)
                result as StatisticsUiState.Success
                assertEquals(
                    expected.totalBooksRead,
                    result.totalBooksRead,
                )
                assertEquals(
                    expected.booksByYearEntries.entries.map { it.key to it.size },
                    result.booksByYearEntries.entries.map { it.key to it.size },
                )
                assertEquals(
                    expected.booksByMonthEntries.entries.map { it.key to it.size },
                    result.booksByMonthEntries.entries.map { it.key to it.size },
                )
                assertEquals(
                    expected.booksByAuthorStats,
                    result.booksByAuthorStats,
                )
                assertEquals(
                    expected.shorterBook,
                    result.shorterBook,
                )
                assertEquals(
                    expected.longerBook,
                    result.longerBook,
                )
                assertEquals(
                    expected.booksByFormatEntries.entries.map { it.key to it.size },
                    result.booksByFormatEntries.entries.map { it.key to it.size },
                )
                assertEquals(
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
            assertEquals(StatisticsUiState.Empty, awaitItem())

            viewModel.fetchBooks()

            assertEquals(
                StatisticsUiState.Success.empty().copy(isLoading = true),
                awaitItem(),
            )
            assertEquals(
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
            assertEquals(null, awaitItem())

            viewModel.showConfirmationDialog(Res.string.export_confirmation)

            assertEquals(Res.string.export_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN dialog message is not updated`() =
        runTest {
            viewModel.confirmationDialogMessageId.test {
                assertEquals(null, awaitItem())
                viewModel.showConfirmationDialog(Res.string.export_confirmation)
                assertEquals(Res.string.export_confirmation, awaitItem())

                viewModel.showConfirmationDialog(Res.string.export_confirmation)

                expectNoEvents()
            }
        }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        coEvery { booksLocalDataSource.importDataFrom(any()) } throws Exception()
        viewModel.booksError.test {
            val booksError = this
            assertEquals(null, awaitItem())
            viewModel.importData("[]")
            assertEquals(
                ErrorModel("", Res.string.error_file_data),
                awaitItem(),
            )

            viewModel.confirmationDialogMessageId.test {
                val confirmationDialogMessage = this
                assertEquals(null, awaitItem())
                viewModel.showConfirmationDialog(Res.string.export_confirmation)
                assertEquals(
                    Res.string.export_confirmation,
                    confirmationDialogMessage.awaitItem(),
                )

                viewModel.infoDialogMessageId.test {
                    val infoDialogMessage = this
                    assertEquals(null, awaitItem())

                    coEvery { booksLocalDataSource.importDataFrom(any()) } just Runs
                    viewModel.importData("[]")
                    assertEquals(
                        Res.string.data_imported,
                        infoDialogMessage.awaitItem(),
                    )

                    viewModel.closeDialogs()

                    assertEquals(null, booksError.awaitItem())
                    assertEquals(null, confirmationDialogMessage.awaitItem())
                    assertEquals(null, infoDialogMessage.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.booksError.test {
            assertEquals(null, awaitItem())

            viewModel.confirmationDialogMessageId.test {
                assertEquals(null, awaitItem())

                viewModel.infoDialogMessageId.test {
                    assertEquals(null, awaitItem())

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
            assertEquals(null, awaitItem())

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

            assertEquals(Res.string.data_imported, awaitItem())
        }
        coVerify { booksLocalDataSource.importDataFrom(listOf(book.toLocalData())) }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN invalid json data WHEN importData THEN data is saved`() = runTest {
        coEvery { booksLocalDataSource.importDataFrom(any()) } just Runs

        viewModel.booksError.test {
            assertEquals(null, awaitItem())

            viewModel.importData("[{\"titl\":\"\"}]")

            assertEquals(
                ErrorModel("", Res.string.error_file_data),
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
            assertEquals(null, awaitItem())

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

            assertEquals(
                ErrorModel("", Res.string.error_file_data),
                awaitItem(),
            )
        }
        coVerify { booksLocalDataSource.importDataFrom(listOf(book.toLocalData())) }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN books WHEN getDataToExport THEN returns json with books data`() = runTest {
        var json: String? = null
        val book = Book(
            id = "bookId",
            title = "title",
            subtitle = "subtitle",
            authors = listOf("author"),
            publisher = "publisher",
            publishedDate = LocalDate(2025, 10, 5),
            readingDate = null,
            description = "description",
            summary = "summary",
            isbn = "isbn",
            pageCount = 10,
            categories = listOf(GenreResponse("categoryId", "Category")),
            averageRating = 2.0,
            ratingsCount = 5,
            rating = 4.0,
            thumbnail = null,
            image = "image",
            format = "format",
            state = "state",
            priority = 8,
        )
        every { booksLocalDataSource.getAllBooks() } returns flowOf(listOf(book.toLocalData()))

        viewModel.infoDialogMessageId.test {
            assertEquals(null, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            assertEquals(Res.string.file_created, awaitItem())
            assertEquals(
                """
                [{
                    "googleId":"${book.id}",
                    "title" : "${book.title}",
                    "subtitle" : "${book.subtitle}",
                    "authors" : ${book.authors?.map { "\"${it}\"" }},
                    "publisher" : "${book.publisher}",
                    "publishedDate" : "${book.publishedDate.toString("dd/MM/yyyy")}",
                    "description" : "${book.description}",
                    "summary" : "${book.summary}",
                    "isbn" : "${book.isbn}",
                    "pageCount":${book.pageCount},
                    "categories" : ${book.categories?.map { "\"${it.id}\"" }},
                    "averageRating":${book.averageRating},
                    "ratingsCount":${book.ratingsCount},
                    "rating":${book.rating},
                    "image" : "${book.image}",
                    "format" : "${book.format}",
                    "state" : "${book.state}",
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
            assertEquals(null, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            assertEquals(Res.string.file_created, awaitItem())
            assertEquals("[]", json)
        }
        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }

    @Test
    fun `GIVEN failure on getting books WHEN getDataToExport THEN error is shown`() = runTest {
        var json: String? = null
        every { booksLocalDataSource.getAllBooks() } throws Exception()

        viewModel.booksError.test {
            assertEquals(null, awaitItem())

            viewModel.getDataToExport {
                json = it
            }

            assertEquals(
                ErrorModel("", Res.string.error_database),
                awaitItem(),
            )
            assertEquals(null, json)
        }
        verify { booksLocalDataSource.getAllBooks() }
        confirmVerified(booksLocalDataSource)
    }
}