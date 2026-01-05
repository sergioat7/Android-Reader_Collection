/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 8/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.BooksRepositoryImpl
import aragones.sergio.readercollection.data.UserRepositoryImpl
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.account.AccountUiState
import aragones.sergio.readercollection.presentation.account.AccountViewModel
import com.aragones.sergio.BooksLocalDataSource
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AccountViewModelTest {

    private val testUserId = "userId"
    private val testUsername = "userId"
    private val testPassword = ""
    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { userId } returns testUserId
        every { username } returns testUsername
        every { userData } returns UserData(testUsername, testPassword)
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = AccountViewModel(
        BooksRepositoryImpl(
            booksLocalDataSource,
            booksRemoteDataSource,
            ioDispatcher,
        ),
        UserRepositoryImpl(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `WHEN onResume THEN updates state with repository data`() = runTest {
        val userData = UserData("username", "password")
        val isPublicProfile = true
        every { userLocalDataSource.userData } returns userData
        every { userLocalDataSource.isProfilePublic } returns isPublicProfile

        viewModel.state.test {
            val initialValue = AccountUiState.empty().copy(
                username = testUsername,
            )
            assertEquals(initialValue, awaitItem())

            viewModel.onResume()

            assertEquals(
                initialValue.copy(
                    password = userData.password,
                    passwordError = null,
                    isProfilePublic = isPublicProfile,
                ),
                awaitItem(),
            )
        }
        verify { userLocalDataSource.userData }
        verify { userLocalDataSource.isProfilePublic }
    }

    @Test
    fun `GIVEN new password and success response WHEN save THEN updates password`() = runTest {
        val newPassword = "123456"
        viewModel.profileDataChanged(newPassword)
        coEvery { userRemoteDataSource.login(any(), any()) } returns Result.success(testUserId)
        coEvery { userRemoteDataSource.updatePassword(any()) } returns Result.success(Unit)
        every { userLocalDataSource.storePassword(any()) } just Runs
        every { userLocalDataSource.storeCredentials(any()) } just Runs

        viewModel.state.test {
            val initialValue = AccountUiState.empty().copy(
                username = testUsername,
                password = newPassword,
            )
            assertEquals(initialValue, awaitItem())

            viewModel.save()

            assertEquals(
                initialValue.copy(isLoading = true),
                awaitItem(),
            )
            assertEquals(
                initialValue.copy(isLoading = false),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.login(testUsername, testPassword) }
        coVerify { userRemoteDataSource.updatePassword(newPassword) }
        verify { userLocalDataSource.storePassword(newPassword) }
        coVerify { userRemoteDataSource.login(testUsername, newPassword) }
        verify { userLocalDataSource.storeCredentials(AuthData(testUserId)) }
    }

    @Test
    fun `GIVEN new password and login failure WHEN save THEN show error`() = runTest {
        val newPassword = "123456"
        viewModel.profileDataChanged(newPassword)
        coEvery { userRemoteDataSource.login(any(), any()) } returns Result.failure(Exception())

        viewModel.profileError.test {
            assertEquals(null, awaitItem())

            viewModel.save()

            assertEquals(
                ErrorModel(Constants.EMPTY_VALUE, R.string.error_server),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.login(testUsername, testPassword) }
        coVerify(exactly = 0) { userRemoteDataSource.updatePassword(newPassword) }
        verify(exactly = 0) { userLocalDataSource.storePassword(newPassword) }
        coVerify(exactly = 0) { userRemoteDataSource.login(testUsername, newPassword) }
        verify(exactly = 0) { userLocalDataSource.storeCredentials(AuthData(testUserId)) }
    }

    @Test
    fun `GIVEN new password and failure response WHEN save THEN show error`() = runTest {
        val newPassword = "123456"
        viewModel.profileDataChanged(newPassword)
        coEvery { userRemoteDataSource.login(any(), any()) } returns Result.success(testUserId)
        coEvery { userRemoteDataSource.updatePassword(any()) } returns Result.failure(Exception())

        viewModel.profileError.test {
            assertEquals(null, awaitItem())

            viewModel.save()

            assertEquals(
                ErrorModel(Constants.EMPTY_VALUE, R.string.error_server),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.login(testUsername, testPassword) }
        coVerify { userRemoteDataSource.updatePassword(newPassword) }
        verify(exactly = 0) { userLocalDataSource.storePassword(newPassword) }
        coVerify(exactly = 0) { userRemoteDataSource.login(testUsername, newPassword) }
    }

    @Test
    fun `GIVEN same password WHEN save THEN do nothing`() {
        viewModel.save()

        coVerify(exactly = 0) { userRemoteDataSource.updatePassword(any()) }
    }

    @Test
    fun `GIVEN true and success response WHEN setPublicProfile THEN register public profile`() =
        runTest {
            val value = true
            coEvery {
                userRemoteDataSource.registerPublicProfile(
                    any(),
                    any(),
                )
            } returns Result.success(Unit)
            every { userLocalDataSource.storePublicProfile(any()) } just Runs

            viewModel.state.test {
                val initialValue = AccountUiState.empty().copy(
                    username = testUsername,
                )
                assertEquals(initialValue, awaitItem())

                viewModel.setPublicProfile(value)

                assertEquals(initialValue.copy(isLoading = true), awaitItem())
                assertEquals(
                    initialValue.copy(isProfilePublic = value, isLoading = false),
                    awaitItem(),
                )
            }
            coVerify { userRemoteDataSource.registerPublicProfile(testUsername, testUserId) }
            verify { userLocalDataSource.storePublicProfile(value) }
        }

    @Test
    fun `GIVEN false and success response WHEN setPublicProfile THEN delete public profile`() =
        runTest {
            val value = false
            coEvery { userRemoteDataSource.deletePublicProfile(any()) } returns Result.success(Unit)
            every { userLocalDataSource.storePublicProfile(any()) } just Runs

            viewModel.state.test {
                val initialValue = AccountUiState.empty().copy(
                    username = testUsername,
                )
                assertEquals(initialValue, awaitItem())

                viewModel.setPublicProfile(value)

                assertEquals(
                    initialValue.copy(isLoading = true),
                    awaitItem(),
                )
                assertEquals(
                    initialValue.copy(isProfilePublic = value, isLoading = false),
                    awaitItem(),
                )
            }
            coVerify { userRemoteDataSource.deletePublicProfile(testUserId) }
            verify { userLocalDataSource.storePublicProfile(value) }
        }

    @Test
    fun `GIVEN value and failure response WHEN setPublicProfile THEN show error`() = runTest {
        val value = true
        coEvery {
            userRemoteDataSource.registerPublicProfile(any(), any())
        } returns Result.failure(Exception())

        viewModel.profileError.test {
            assertEquals(null, awaitItem())

            viewModel.setPublicProfile(value)

            assertEquals(
                ErrorModel(Constants.EMPTY_VALUE, R.string.error_server),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.registerPublicProfile(testUsername, testUserId) }
    }

    @Test
    fun `GIVEN success response WHEN deleteUser THEN all books are removed and logs out`() =
        runTest {
            coEvery { userRemoteDataSource.login(any(), any()) } returns Result.success(testUserId)
            coEvery { userRemoteDataSource.deleteUser(any()) } returns Result.success(Unit)
            every { userLocalDataSource.logout() } just Runs
            every { userLocalDataSource.removeUserData() } just Runs
            val book = Book("id")
            every { booksLocalDataSource.getAllBooks() } returns flowOf(listOf(book.toLocalData()))
            coEvery { booksLocalDataSource.deleteBooks(any()) } just Runs

            viewModel.logOut.test {
                assertEquals(false, awaitItem())

                viewModel.deleteUser()

                assertEquals(true, awaitItem())
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.deleteBooks(listOf(book.toLocalData())) }
            coVerify { userLocalDataSource.logout() }
            coVerify { userLocalDataSource.removeUserData() }
            coVerify { userRemoteDataSource.deleteUser(testUserId) }
            coVerify { userRemoteDataSource.login(testUsername, testPassword) }
        }

    @Test
    fun `GIVEN delete books failure response WHEN deleteUser THEN books are not removed and logs out`() =
        runTest {
            coEvery { userRemoteDataSource.login(any(), any()) } returns Result.success(testUserId)
            coEvery { userRemoteDataSource.deleteUser(any()) } returns Result.success(Unit)
            every { userLocalDataSource.logout() } just Runs
            every { userLocalDataSource.removeUserData() } just Runs
            every { booksLocalDataSource.getAllBooks() } returns flowOf(emptyList())
            coEvery { booksLocalDataSource.deleteBooks(any()) } throws Exception()

            viewModel.logOut.test {
                assertEquals(false, awaitItem())

                viewModel.deleteUser()

                assertEquals(true, awaitItem())
            }
            verify { booksLocalDataSource.getAllBooks() }
            coVerify { booksLocalDataSource.deleteBooks(any()) }
            coVerify { userLocalDataSource.logout() }
            coVerify { userLocalDataSource.removeUserData() }
            coVerify { userRemoteDataSource.deleteUser(testUserId) }
            coVerify { userRemoteDataSource.login(testUsername, testPassword) }
        }

    @Test
    fun `GIVEN delete user failure response WHEN deleteUser THEN show error`() = runTest {
        coEvery { userRemoteDataSource.login(any(), any()) } returns Result.success(testUserId)
        coEvery { userRemoteDataSource.deleteUser(any()) } returns Result.failure(Exception())

        viewModel.profileError.test {
            assertEquals(null, awaitItem())

            viewModel.deleteUser()

            assertEquals(
                ErrorModel(Constants.EMPTY_VALUE, R.string.error_server),
                awaitItem(),
            )
        }
        coVerify { userRemoteDataSource.deleteUser(testUserId) }
        coVerify { userRemoteDataSource.login(testUsername, testPassword) }
    }

    @Test
    fun `GIVEN valid password WHEN profileDataChanged THEN update state with new password`() =
        runTest {
            val newPassword = "123456"

            viewModel.state.test {
                val initialValue = AccountUiState.empty().copy(
                    username = testUsername,
                )
                assertEquals(initialValue, awaitItem())

                viewModel.profileDataChanged(newPassword)

                assertEquals(
                    initialValue.copy(password = newPassword, passwordError = null),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `GIVEN invalid password WHEN profileDataChanged THEN update state with password error`() =
        runTest {
            val newPassword = "123"

            viewModel.state.test {
                val initialValue = AccountUiState.empty().copy(
                    username = testUsername,
                )
                assertEquals(initialValue, awaitItem())

                viewModel.profileDataChanged(newPassword)

                assertEquals(
                    initialValue.copy(
                        password = newPassword,
                        passwordError = R.string.invalid_password,
                    ),
                    awaitItem(),
                )
            }
        }

    @Test
    fun `GIVEN no dialog shown WHEN showConfirmationDialog THEN dialog is shown`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)

            assertEquals(R.string.profile_delete_confirmation, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showConfirmationDialog THEN do nothing`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)
            assertEquals(R.string.profile_delete_confirmation, awaitItem())

            viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN showInfoDialog THEN dialog is shown`() = runTest {
        viewModel.infoDialogMessageId.test {
            assertEquals(-1, awaitItem())

            viewModel.showInfoDialog(R.string.username_info)

            assertEquals(R.string.username_info, awaitItem())
        }
    }

    @Test
    fun `GIVEN same dialog message shown WHEN showInfoDialog THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            assertEquals(-1, awaitItem())
            viewModel.showInfoDialog(R.string.username_info)
            assertEquals(R.string.username_info, awaitItem())

            viewModel.showInfoDialog(R.string.username_info)

            expectNoEvents()
        }
    }

    @Test
    fun `GIVEN dialog shown WHEN closeDialogs THEN dialog is reset`() = runTest {
        viewModel.confirmationDialogMessageId.test {
            val confirmationDialogMessage = this
            assertEquals(-1, awaitItem())
            viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)
            assertEquals(
                R.string.profile_delete_confirmation,
                confirmationDialogMessage.awaitItem(),
            )

            viewModel.infoDialogMessageId.test {
                val infoDialogMessage = this
                assertEquals(-1, awaitItem())
                viewModel.showInfoDialog(R.string.username_info)
                assertEquals(
                    R.string.username_info,
                    infoDialogMessage.awaitItem(),
                )

                viewModel.profileError.test {
                    val profileError = this
                    assertEquals(null, awaitItem())
                    coEvery {
                        userRemoteDataSource.login(
                            any(),
                            any(),
                        )
                    } returns Result.success(testUserId)
                    coEvery {
                        userRemoteDataSource.deleteUser(
                            any(),
                        )
                    } returns Result.failure(Exception())
                    viewModel.deleteUser()
                    assertEquals(
                        ErrorModel(Constants.EMPTY_VALUE, R.string.error_server),
                        awaitItem(),
                    )

                    viewModel.closeDialogs()

                    assertEquals(-1, confirmationDialogMessage.awaitItem())
                    assertEquals(-1, infoDialogMessage.awaitItem())
                    assertEquals(null, profileError.awaitItem())
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

                viewModel.profileError.test {
                    assertEquals(null, awaitItem())

                    viewModel.closeDialogs()

                    expectNoEvents()
                }
            }
        }
    }
}