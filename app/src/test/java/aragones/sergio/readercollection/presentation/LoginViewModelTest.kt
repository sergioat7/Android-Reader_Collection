/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/9/2025
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
import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.toDomain
import aragones.sergio.readercollection.domain.toLocalData
import aragones.sergio.readercollection.presentation.login.LoginViewModel
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class LoginViewModelTest {

    private val testUsername = "user"
    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { username } returns testUsername
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = LoginViewModel(
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
    fun `GIVEN valid username and password WHEN login THEN user config and books are loaded and true is returned`() =
        runTest {
            val password = "pass"
            val userId = "userId"
            val userData = UserData(testUsername, password)
            val authData = AuthData(userId)
            val isActive = true
            val books = listOf(BookResponse("1"), BookResponse("2"))
            val domainBooks = books.map { it.toDomain().toLocalData() }
            coEvery {
                userRemoteDataSource.login(
                    testUsername,
                    password,
                )
            } returns Result.success(userId)
            every { userLocalDataSource.storeLoginData(userData, authData) } just Runs
            coEvery {
                userRemoteDataSource.isPublicProfileActive(testUsername)
            } returns Result.success(isActive)
            every { userLocalDataSource.storePublicProfile(isActive) } just Runs
            every { userLocalDataSource.userId } returns userId
            coEvery { booksRemoteDataSource.getBooks(userId) } returns Result.success(books)
            coEvery { booksLocalDataSource.insertBooks(domainBooks) } just Runs

            viewModel.loginSuccess.test {
                assertEquals(false, awaitItem())

                viewModel.login(testUsername, password)

                assertEquals(true, awaitItem())
            }

            verify(exactly = 2) { userLocalDataSource.username }
            coVerify { userRemoteDataSource.login(testUsername, password) }
            verify { userLocalDataSource.storeLoginData(userData, authData) }
            coVerify { userRemoteDataSource.isPublicProfileActive(testUsername) }
            verify { userLocalDataSource.storePublicProfile(isActive) }
            verify { userLocalDataSource.userId }
            coVerify { booksRemoteDataSource.getBooks(userId) }
            coVerify { booksLocalDataSource.insertBooks(domainBooks) }
            confirmVerified(
                booksLocalDataSource,
                booksRemoteDataSource,
                userLocalDataSource,
                userRemoteDataSource,
            )
        }

    @Test
    fun `GIVEN load books failure response WHEN login THEN user config is loaded but then deleted and error is shown`() =
        runTest {
            val password = "pass"
            val userId = "userId"
            val userData = UserData(testUsername, password)
            val authData = AuthData(userId)
            val isActive = true
            val books = listOf(BookResponse("1"), BookResponse("2"))
            val domainBooks = books.map { it.toDomain().toLocalData() }
            val exception = RuntimeException("Firestore error")
            coEvery {
                userRemoteDataSource.login(
                    testUsername,
                    password,
                )
            } returns Result.success(userId)
            every { userLocalDataSource.storeLoginData(userData, authData) } just Runs
            coEvery {
                userRemoteDataSource.isPublicProfileActive(testUsername)
            } returns Result.success(isActive)
            every { userLocalDataSource.storePublicProfile(isActive) } just Runs
            every { userLocalDataSource.userId } returns userId
            coEvery { booksRemoteDataSource.getBooks(userId) } returns Result.failure(exception)
            coEvery { booksLocalDataSource.insertBooks(domainBooks) } just Runs
            every { userLocalDataSource.logout() } just Runs
            every { userRemoteDataSource.logout() } just Runs

            viewModel.loginError.test {
                assertEquals(null, awaitItem())

                viewModel.login(testUsername, password)

                assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.error_server,
                    ),
                    awaitItem(),
                )
            }

            verify(exactly = 2) { userLocalDataSource.username }
            coVerify { userRemoteDataSource.login(testUsername, password) }
            verify { userLocalDataSource.storeLoginData(userData, authData) }
            coVerify { userRemoteDataSource.isPublicProfileActive(testUsername) }
            verify { userLocalDataSource.storePublicProfile(isActive) }
            verify { userLocalDataSource.userId }
            coVerify { booksRemoteDataSource.getBooks(userId) }
            coVerify(exactly = 0) { booksLocalDataSource.insertBooks(domainBooks) }
            verify { userLocalDataSource.logout() }
            verify { userRemoteDataSource.logout() }
            confirmVerified(
                booksLocalDataSource,
                booksRemoteDataSource,
                userLocalDataSource,
                userRemoteDataSource,
            )
        }

    @Test
    fun `GIVEN wrong username or password WHEN login THEN login data is not stored and error is shown`() =
        runTest {
            val password = "pass"
            val exception = RuntimeException("Firestore error")
            coEvery {
                userRemoteDataSource.login(
                    testUsername,
                    password,
                )
            } returns Result.failure(exception)

            viewModel.loginError.test {
                assertEquals(null, awaitItem())

                viewModel.login(testUsername, password)

                assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.wrong_credentials,
                    ),
                    awaitItem(),
                )
            }

            verify { userLocalDataSource.username }
            coVerify { userRemoteDataSource.login(testUsername, password) }
            verify(exactly = 0) { userLocalDataSource.storeLoginData(any(), any()) }
            confirmVerified(
                booksLocalDataSource,
                booksRemoteDataSource,
                userLocalDataSource,
                userRemoteDataSource,
            )
        }

    @Test
    fun `GIVEN valid username and password WHEN loginDataChanged THEN state updates with data valid true`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.loginDataChanged("username", "password")

        assertEquals(
            LoginFormState(
                usernameError = null,
                passwordError = null,
                isDataValid = true,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN invalid username WHEN loginDataChanged THEN state updates with data valid false and username error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.loginDataChanged("", "password")

        assertEquals(
            LoginFormState(
                usernameError = R.string.invalid_username,
                passwordError = null,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN invalid password WHEN loginDataChanged THEN state updates with data valid false and password error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.loginDataChanged("username", "pass")

        assertEquals(
            LoginFormState(
                usernameError = null,
                passwordError = R.string.invalid_password,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN invalid username and password WHEN loginDataChanged THEN state updates with data valid false and username and password errors`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.loginDataChanged("", "pass")

        assertEquals(
            LoginFormState(
                usernameError = R.string.invalid_username,
                passwordError = R.string.invalid_password,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN error dialog shown WHEN closeDialogs THEN error dialog is reset`() = runTest {
        val password = "pass"
        val exception = RuntimeException("Firestore error")
        coEvery {
            userRemoteDataSource.login(
                testUsername,
                password,
            )
        } returns Result.failure(exception)

        viewModel.loginError.test {
            assertEquals(null, awaitItem())
            viewModel.login(testUsername, password)
            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.wrong_credentials,
                ),
                awaitItem(),
            )

            viewModel.closeDialogs()

            assertEquals(null, awaitItem())
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.loginError.test {
            assertEquals(null, awaitItem())

            viewModel.closeDialogs()

            expectNoEvents()
        }
    }
}