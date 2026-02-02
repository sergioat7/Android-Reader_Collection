/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/9/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.data.UserRepositoryImpl
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.register.RegisterViewModel
import aragones.sergio.readercollection.presentation.utils.MainDispatcherRule
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
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.error_server
import reader_collection.app.generated.resources.error_user_found
import reader_collection.app.generated.resources.invalid_password
import reader_collection.app.generated.resources.invalid_repeat_password
import reader_collection.app.generated.resources.invalid_username
import reader_collection.app.generated.resources.username_info

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testUsername = "user"
    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { username } returns testUsername
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val viewModel = RegisterViewModel(
        UserRepositoryImpl(
            userLocalDataSource,
            userRemoteDataSource,
            mainDispatcherRule.testDispatcher,
        ),
    )

    @Test
    fun `GIVEN valid username and password WHEN register THEN user is registered and logged in and true is returned`() =
        runTest {
            val password = "pass"
            val userId = "userId"
            val userData = UserData(testUsername, password)
            val authData = AuthData(userId)
            coEvery {
                userRemoteDataSource.register(
                    testUsername,
                    password,
                )
            } returns Result.success(Unit)
            coEvery {
                userRemoteDataSource.login(
                    testUsername,
                    password,
                )
            } returns Result.success(userId)
            every { userLocalDataSource.storeLoginData(userData, authData) } just Runs

            viewModel.registerSuccess.test {
                assertEquals(false, awaitItem())

                viewModel.register(testUsername, password)

                assertEquals(true, awaitItem())
            }

            coVerify { userRemoteDataSource.register(testUsername, password) }
            coVerify { userRemoteDataSource.login(testUsername, password) }
            verify { userLocalDataSource.storeLoginData(userData, authData) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN login error WHEN register THEN login data is not stored and error is shown`() =
        runTest {
            val password = "pass"
            val exception = RuntimeException("Firestore error")
            coEvery {
                userRemoteDataSource.register(
                    testUsername,
                    password,
                )
            } returns Result.success(Unit)
            coEvery {
                userRemoteDataSource.login(
                    testUsername,
                    password,
                )
            } returns Result.failure(exception)

            viewModel.registerError.test {
                assertEquals(null, awaitItem())

                viewModel.register(testUsername, password)

                assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        Res.string.error_server,
                    ),
                    awaitItem(),
                )
            }

            coVerify { userRemoteDataSource.register(testUsername, password) }
            coVerify { userRemoteDataSource.login(testUsername, password) }
            verify(exactly = 0) { userLocalDataSource.storeLoginData(any(), any()) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user already registered WHEN register THEN error is shown`() = runTest {
        val password = "pass"
        val userData = UserData(testUsername, password)
        val authData = AuthData("")
        val exception = CustomExceptions.ExistentUser()
        coEvery {
            userRemoteDataSource.register(
                testUsername,
                password,
            )
        } returns Result.failure(exception)

        viewModel.registerError.test {
            assertEquals(null, awaitItem())

            viewModel.register(testUsername, password)

            assertEquals(
                ErrorModel(
                    Constants.EMPTY_VALUE,
                    Res.string.error_user_found,
                ),
                awaitItem(),
            )
        }

        coVerify { userRemoteDataSource.register(testUsername, password) }
        verify(exactly = 0) { userLocalDataSource.storeLoginData(userData, authData) }
        confirmVerified(userLocalDataSource, userRemoteDataSource)
    }

    @Test
    fun `GIVEN valid username and passwords WHEN registerDataChanged THEN state updates with data valid true`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.registerDataChanged(
            username = "username",
            password = "password",
            confirmPassword = "password",
        )

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
    fun `GIVEN invalid username WHEN registerDataChanged THEN state updates with data valid false and username error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.registerDataChanged(
            username = "",
            password = "password",
            confirmPassword = "password",
        )

        assertEquals(
            LoginFormState(
                usernameError = Res.string.invalid_username,
                passwordError = null,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN invalid password WHEN registerDataChanged THEN state updates with data valid false and password error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.registerDataChanged(
            username = "username",
            password = "pass",
            confirmPassword = "pass",
        )

        assertEquals(
            LoginFormState(
                usernameError = null,
                passwordError = Res.string.invalid_password,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN mismatch passwords WHEN registerDataChanged THEN state updates with data valid false and password error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.registerDataChanged(
            username = "username",
            password = "password1",
            confirmPassword = "password2",
        )

        assertEquals(
            LoginFormState(
                usernameError = null,
                passwordError = Res.string.invalid_repeat_password,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN invalid username and mismatch passwords WHEN registerDataChanged THEN state updates with data valid false and username and password error`() {
        assertEquals(
            LoginFormState(),
            viewModel.uiState.value.formState,
        )

        viewModel.registerDataChanged(
            username = "",
            password = "password1",
            confirmPassword = "password2",
        )

        assertEquals(
            LoginFormState(
                usernameError = Res.string.invalid_username,
                passwordError = Res.string.invalid_repeat_password,
                isDataValid = false,
            ),
            viewModel.uiState.value.formState,
        )
    }

    @Test
    fun `GIVEN no dialog shown WHEN showInfoDialog THEN dialog is shown`() = runTest {
        val textId = Res.string.username_info
        viewModel.infoDialogMessageId.test {
            assertEquals(null, awaitItem())

            viewModel.showInfoDialog(textId)

            assertEquals(textId, awaitItem())
        }
    }

    @Test
    fun `GIVEN error dialog shown WHEN closeDialogs THEN error dialog is reset`() = runTest {
        val password = "pass"
        val exception = RuntimeException("Firestore error")
        coEvery {
            userRemoteDataSource.register(
                testUsername,
                password,
            )
        } returns Result.failure(exception)

        viewModel.infoDialogMessageId.test {
            val infoDialogMessageId = this
            assertEquals(null, infoDialogMessageId.awaitItem())
            viewModel.registerError.test {
                val registerError = this
                assertEquals(null, registerError.awaitItem())

                viewModel.register(testUsername, password)
                viewModel.showInfoDialog(Res.string.username_info)
                assertEquals(Res.string.username_info, infoDialogMessageId.awaitItem())
                assertEquals(
                    ErrorModel(
                        Constants.EMPTY_VALUE,
                        Res.string.error_server,
                    ),
                    registerError.awaitItem(),
                )

                viewModel.closeDialogs()

                assertEquals(null, infoDialogMessageId.awaitItem())
                assertEquals(null, registerError.awaitItem())
            }
        }
    }

    @Test
    fun `GIVEN no dialog shown WHEN closeDialogs THEN do nothing`() = runTest {
        viewModel.infoDialogMessageId.test {
            assertEquals(null, awaitItem())
            viewModel.registerError.test {
                assertEquals(null, awaitItem())

                viewModel.closeDialogs()

                expectNoEvents()
            }
        }
    }
}