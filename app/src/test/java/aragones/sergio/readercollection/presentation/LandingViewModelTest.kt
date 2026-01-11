/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/9/2025
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
import aragones.sergio.readercollection.presentation.landing.LandingViewModel
import com.aragones.sergio.BooksLocalDataSource
import io.mockk.Called
import io.mockk.Runs
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

class LandingViewModelTest {

    private val booksLocalDataSource: BooksLocalDataSource = mockk()
    private val booksRemoteDataSource: BooksRemoteDataSource = mockk()
    private val userLocalDataSource: UserLocalDataSource = mockk()
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = LandingViewModel(
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
    fun `WHEN get language THEN return language`() {
        val language = "en"
        every { userLocalDataSource.language } returns language

        val result = viewModel.language

        assertEquals(language, result)
        verify { userLocalDataSource.language }
        confirmVerified(userLocalDataSource)
    }

    @Test
    fun `GIVEN user logged in and exist user WHEN check if is logged in THEN return true`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns true
            every { userRemoteDataSource.userExists } returns true
            viewModel.isLogged.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(true, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.userExists }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user logged in and not exist user WHEN check if is logged in THEN return false`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns true
            every { userRemoteDataSource.userExists } returns false
            viewModel.isLogged.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(false, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.userExists }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user not logged in with current user not null WHEN check if is logged in THEN return false`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns false
            viewModel.isLogged.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(false, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.wasNot(Called) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user not logged in with current user null WHEN check if is logged in THEN return false`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns false
            viewModel.isLogged.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(false, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.wasNot(Called) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `WHEN check theme THEN userLocalDataSource is called`() {
        every { userLocalDataSource.applyTheme() } just Runs

        viewModel.checkTheme()

        verify { userLocalDataSource.applyTheme() }
        confirmVerified(userLocalDataSource)
    }

    @Test
    fun `GIVEN language WHEN fetch remote config values THEN remote config values are fetched for that language`() {
        val language = "en"
        every { userLocalDataSource.language } returns language
        every { booksRemoteDataSource.fetchRemoteConfigValues(language) } just Runs

        viewModel.fetchRemoteConfigValues()

        verify { booksRemoteDataSource.fetchRemoteConfigValues(language) }
        confirmVerified(booksRemoteDataSource)
    }

    @Test
    fun `fetchRemoteConfigValues   Calls repository method`() {
        val language = "es"
        every { userLocalDataSource.language = language } just Runs

        viewModel.setLanguage(language)

        verify { userLocalDataSource.language = language }
        confirmVerified(userLocalDataSource)
    }
}