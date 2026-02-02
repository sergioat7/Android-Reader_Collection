/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/10/2025
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
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.datasync.DataSyncUiState
import aragones.sergio.readercollection.presentation.datasync.DataSyncViewModel
import aragones.sergio.readercollection.presentation.utils.MainDispatcherRule
import com.aragones.sergio.BooksLocalDataSource
import com.aragones.sergio.util.Constants
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.data_sync_successfully
import reader_collection.app.generated.resources.error_server
import reader_collection.app.generated.resources.export_confirmation
import reader_collection.app.generated.resources.sync_confirmation

class DataSyncViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testUserId = "userId"
    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { isAutomaticSyncEnabled } returns false
        every { userId } returns testUserId
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val viewModel = DataSyncViewModel(
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
    fun `GIVEN value WHEN changeAutomaticSync THEN value is updated`() {
        val value = true
        every { userLocalDataSource.storeAutomaticSync(any()) } just Runs

        viewModel.changeAutomaticSync(value)

        assertEquals(
            DataSyncUiState(
                isAutomaticSyncEnabled = value,
                isLoading = false,
            ),
            viewModel.state.value,
        )
        verify { userLocalDataSource.isAutomaticSyncEnabled }
        verify { userLocalDataSource.storeAutomaticSync(value) }
        confirmVerified(userLocalDataSource)
    }

    @Test
    fun `GIVEN new books and out-of-date books WHEN syncData THEN add new books and remove out-of-date books and show success message`() =
        runTest {
            val newBook = Book("bookId1")
            val outOfDateBook = Book("bookId2")
            coEvery { booksRemoteDataSource.getBooks(any()) } returns Result.success(
                listOf(
                    outOfDateBook.toRemoteData(),
                ),
            )
            coEvery { booksLocalDataSource.getAllBooks() } returns flowOf(
                listOf(newBook.toLocalData()),
            )
            coEvery {
                booksRemoteDataSource.syncBooks(any(), any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                assertEquals(null, awaitItem())

                viewModel.syncData()

                assertEquals(Res.string.data_sync_successfully, awaitItem())
            }
            verify { userLocalDataSource.isAutomaticSyncEnabled }
            verify { userLocalDataSource.userId }
            coVerify { booksRemoteDataSource.getBooks(any()) }
            coVerify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksRemoteDataSource.syncBooks(
                    uuid = testUserId,
                    booksToSave = listOf(newBook.toRemoteData()),
                    booksToRemove = listOf(outOfDateBook.toRemoteData()),
                )
            }
            confirmVerified(booksRemoteDataSource, userLocalDataSource)
        }

    @Test
    fun `GIVEN new books but no out-of-date books WHEN syncData THEN add just new books and show success message`() =
        runTest {
            val newBook = Book("bookId")
            coEvery { booksRemoteDataSource.getBooks(any()) } returns Result.success(emptyList())
            coEvery { booksLocalDataSource.getAllBooks() } returns flowOf(
                listOf(newBook.toLocalData()),
            )
            coEvery {
                booksRemoteDataSource.syncBooks(any(), any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                assertEquals(null, awaitItem())

                viewModel.syncData()

                assertEquals(Res.string.data_sync_successfully, awaitItem())
            }
            verify { userLocalDataSource.isAutomaticSyncEnabled }
            verify { userLocalDataSource.userId }
            coVerify { booksRemoteDataSource.getBooks(any()) }
            coVerify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksRemoteDataSource.syncBooks(
                    uuid = testUserId,
                    booksToSave = listOf(newBook.toRemoteData()),
                    booksToRemove = emptyList(),
                )
            }
            confirmVerified(booksRemoteDataSource, userLocalDataSource)
        }

    @Test
    fun `GIVEN no new books and out-of-date books WHEN syncData THEN remove just out-of-date books and show success message`() =
        runTest {
            val outOfDateBook = Book("bookId")
            coEvery { booksRemoteDataSource.getBooks(any()) } returns Result.success(
                listOf(
                    outOfDateBook.toRemoteData(),
                ),
            )
            coEvery { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())
            coEvery {
                booksRemoteDataSource.syncBooks(any(), any(), any())
            } returns Result.success(Unit)

            viewModel.infoDialogMessageId.test {
                assertEquals(null, awaitItem())

                viewModel.syncData()

                assertEquals(Res.string.data_sync_successfully, awaitItem())
            }
            verify { userLocalDataSource.isAutomaticSyncEnabled }
            verify { userLocalDataSource.userId }
            coVerify { booksRemoteDataSource.getBooks(any()) }
            coVerify { booksLocalDataSource.getAllBooks() }
            coVerify {
                booksRemoteDataSource.syncBooks(
                    uuid = testUserId,
                    booksToSave = emptyList(),
                    booksToRemove = listOf(outOfDateBook.toRemoteData()),
                )
            }
            confirmVerified(booksRemoteDataSource, userLocalDataSource)
        }

    @Test
    fun `GIVEN failure WHEN syncData THEN show error message`() = runTest {
        coEvery { booksRemoteDataSource.getBooks(any()) } returns Result.success(emptyList())
        coEvery { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())
        coEvery {
            booksRemoteDataSource.syncBooks(any(), any(), any())
        } returns Result.failure(Exception())

        viewModel.error.test {
            assertEquals(null, awaitItem())

            viewModel.syncData()

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    Res.string.error_server,
                ),
                awaitItem(),
            )
        }
        verify { userLocalDataSource.isAutomaticSyncEnabled }
        verify { userLocalDataSource.userId }
        coVerify { booksRemoteDataSource.getBooks(any()) }
        coVerify { booksLocalDataSource.getAllBooks() }
        coVerify {
            booksRemoteDataSource.syncBooks(
                uuid = testUserId,
                booksToSave = emptyList(),
                booksToRemove = emptyList(),
            )
        }
        confirmVerified(booksRemoteDataSource, userLocalDataSource)
    }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(null, awaitItem())

            viewModel.showConfirmationDialog(Res.string.sync_confirmation)

            assertEquals(Res.string.sync_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN dialog message is not updated`() =
        runTest {
            viewModel.confirmationDialogMessageId.test {
                assertEquals(null, awaitItem())
                viewModel.showConfirmationDialog(Res.string.sync_confirmation)
                assertEquals(Res.string.sync_confirmation, awaitItem())

                viewModel.showConfirmationDialog(Res.string.sync_confirmation)

                expectNoEvents()
            }
        }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        coEvery { booksRemoteDataSource.getBooks(any()) } returns Result.success(emptyList())
        coEvery { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())
        coEvery {
            booksRemoteDataSource.syncBooks(any(), any(), any())
        } returns Result.success(Unit)

        viewModel.infoDialogMessageId.test {
            val infoDialogMessage = this
            assertEquals(null, awaitItem())

            viewModel.syncData()
            assertEquals(
                Res.string.data_sync_successfully,
                infoDialogMessage.awaitItem(),
            )

            viewModel.confirmationDialogMessageId.test {
                val confirmationDialogMessage = this
                assertEquals(null, awaitItem())
                viewModel.showConfirmationDialog(Res.string.export_confirmation)
                assertEquals(
                    Res.string.export_confirmation,
                    confirmationDialogMessage.awaitItem(),
                )
                viewModel.error.test {
                    val error = this
                    assertEquals(null, awaitItem())
                    coEvery {
                        booksRemoteDataSource.syncBooks(any(), any(), any())
                    } returns Result.failure(Exception())
                    viewModel.syncData()
                    assertEquals(
                        ErrorModel(
                            Constants.EMPTY_VALUE,
                            Res.string.error_server,
                        ),
                        awaitItem(),
                    )

                    viewModel.closeDialogs()

                    assertEquals(null, infoDialogMessage.awaitItem())
                    assertEquals(null, confirmationDialogMessage.awaitItem())
                    assertEquals(null, error.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            assertEquals(null, awaitItem())

            viewModel.confirmationDialogMessageId.test {
                assertEquals(null, awaitItem())

                viewModel.error.test {
                    assertEquals(null, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }
}