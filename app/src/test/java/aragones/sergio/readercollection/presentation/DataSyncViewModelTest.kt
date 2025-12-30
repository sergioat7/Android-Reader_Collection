/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/10/2025
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
import aragones.sergio.readercollection.domain.toRemoteData
import aragones.sergio.readercollection.presentation.datasync.DataSyncUiState
import aragones.sergio.readercollection.presentation.datasync.DataSyncViewModel
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DataSyncViewModelTest {

    private val testUserId = "userId"
    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { isAutomaticSyncEnabled } returns false
        every { userId } returns testUserId
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = DataSyncViewModel(
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
    fun `GIVEN value WHEN changeAutomaticSync THEN value is updated`() {
        val value = true
        every { userLocalDataSource.storeAutomaticSync(any()) } just Runs

        viewModel.changeAutomaticSync(value)

        Assert.assertEquals(
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
                Assert.assertEquals(-1, awaitItem())

                viewModel.syncData()

                Assert.assertEquals(R.string.data_sync_successfully, awaitItem())
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
                Assert.assertEquals(-1, awaitItem())

                viewModel.syncData()

                Assert.assertEquals(R.string.data_sync_successfully, awaitItem())
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
                Assert.assertEquals(-1, awaitItem())

                viewModel.syncData()

                Assert.assertEquals(R.string.data_sync_successfully, awaitItem())
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
            Assert.assertEquals(null, awaitItem())

            viewModel.syncData()

            Assert.assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
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
            Assert.assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.sync_confirmation)

            Assert.assertEquals(R.string.sync_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN dialog message is not updated`() =
        runTest {
            viewModel.confirmationDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())
                viewModel.showConfirmationDialog(R.string.sync_confirmation)
                Assert.assertEquals(R.string.sync_confirmation, awaitItem())

                viewModel.showConfirmationDialog(R.string.sync_confirmation)

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
            Assert.assertEquals(-1, awaitItem())

            viewModel.syncData()
            Assert.assertEquals(
                R.string.data_sync_successfully,
                infoDialogMessage.awaitItem(),
            )

            viewModel.confirmationDialogMessageId.test {
                val confirmationDialogMessage = this
                Assert.assertEquals(-1, awaitItem())
                viewModel.showConfirmationDialog(R.string.export_confirmation)
                Assert.assertEquals(
                    R.string.export_confirmation,
                    confirmationDialogMessage.awaitItem(),
                )
                viewModel.error.test {
                    val error = this
                    Assert.assertEquals(null, awaitItem())
                    coEvery {
                        booksRemoteDataSource.syncBooks(any(), any(), any())
                    } returns Result.failure(Exception())
                    viewModel.syncData()
                    Assert.assertEquals(
                        ErrorModel(
                            Constants.EMPTY_VALUE,
                            R.string.error_server,
                        ),
                        awaitItem(),
                    )

                    viewModel.closeDialogs()

                    Assert.assertEquals(-1, infoDialogMessage.awaitItem())
                    Assert.assertEquals(-1, confirmationDialogMessage.awaitItem())
                    Assert.assertEquals(null, error.awaitItem())
                }
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.confirmationDialogMessageId.test {
                Assert.assertEquals(-1, awaitItem())

                viewModel.error.test {
                    Assert.assertEquals(null, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }
}