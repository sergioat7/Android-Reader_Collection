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
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.settings.SettingsViewModel
import com.aragones.sergio.BooksLocalDataSource
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

class SettingsViewModelTest {

    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk()
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = SettingsViewModel(
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
    fun `WHEN logout THEN data sources are invoked and logOut state returns true`() = runTest {
        val books = listOf(
            Book(id = "bookId1").toLocalData(),
            Book(id = "bookId2").toLocalData(),
        )
        every { userLocalDataSource.logout() } just Runs
        every { userRemoteDataSource.logout() } just Runs
        every { booksLocalDataSource.getAllBooks() } returns flowOf(books)
        coEvery { booksLocalDataSource.deleteBooks(any()) } just Runs
        viewModel.logOut.test {
            Assert.assertEquals(false, awaitItem())

            viewModel.logout()

            Assert.assertEquals(true, awaitItem())
        }
        verify { userLocalDataSource.logout() }
        verify { userRemoteDataSource.logout() }
        verify { booksLocalDataSource.getAllBooks() }
        coVerify { booksLocalDataSource.deleteBooks(books) }
        confirmVerified(
            userLocalDataSource,
            userRemoteDataSource,
            booksLocalDataSource,
        )
    }

    @Test
    fun `GIVEN error on reset database WHEN logout THEN data sources are invoked and logOut state returns true`() =
        runTest {
            val books = listOf(
                Book(id = "bookId1").toLocalData(),
                Book(id = "bookId2").toLocalData(),
            )
            every { userLocalDataSource.logout() } just Runs
            every { userRemoteDataSource.logout() } just Runs
            every { booksLocalDataSource.getAllBooks() } returns flowOf(books)
            coEvery {
                booksLocalDataSource.deleteBooks(any())
            } throws RuntimeException("Database error")
            viewModel.logOut.test {
                Assert.assertEquals(false, awaitItem())

                viewModel.logout()

                Assert.assertEquals(true, awaitItem())
            }
            verify { userLocalDataSource.logout() }
            verify { userRemoteDataSource.logout() }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.deleteBooks(books) }
            confirmVerified(
                userLocalDataSource,
                userRemoteDataSource,
                booksLocalDataSource,
            )
        }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)

            Assert.assertEquals(R.string.profile_logout_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)
            Assert.assertEquals(R.string.profile_logout_confirmation, awaitItem())

            viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog message is reset`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)
            Assert.assertEquals(R.string.profile_logout_confirmation, awaitItem())

            viewModel.closeDialogs()

            Assert.assertEquals(-1, awaitItem())
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            Assert.assertEquals(-1, awaitItem())

            viewModel.closeDialogs()

            expectNoEvents()
        }
    }
}