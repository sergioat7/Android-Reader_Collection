/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/9/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import androidx.appcompat.app.AppCompatDelegate
import app.cash.turbine.test
import aragones.sergio.readercollection.data.BooksRepositoryImpl
import aragones.sergio.readercollection.data.UserRepositoryImpl
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.BooksRemoteDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.presentation.landing.LandingViewModel
import aragones.sergio.readercollection.presentation.login.LoginActivity
import com.aragones.sergio.BooksLocalDataSource
import io.mockk.Called
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
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
    fun `GIVEN user logged in and exist user WHEN check if is logged in THEN class launched is MainActivity`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns true
            every { userRemoteDataSource.userExists } returns true
            viewModel.landingClassToStart.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(MainActivity::class.java, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.userExists }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user logged in and not exist user WHEN check if is logged in THEN class launched is LoginActivity`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns true
            every { userRemoteDataSource.userExists } returns false
            viewModel.landingClassToStart.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(LoginActivity::class.java, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.userExists }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user not logged in with current user not null WHEN check if is logged in THEN class launched is LoginActivity`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns false
            viewModel.landingClassToStart.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(LoginActivity::class.java, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.wasNot(Called) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN user not logged in with current user null WHEN check if is logged in THEN class launched is LoginActivity`() =
        runTest {
            every { userLocalDataSource.isLoggedIn } returns false
            viewModel.landingClassToStart.test {
                assertEquals(null, awaitItem())

                viewModel.checkIsLoggedIn()

                assertEquals(LoginActivity::class.java, awaitItem())
            }
            verify { userLocalDataSource.isLoggedIn }
            verify { userRemoteDataSource.wasNot(Called) }
            confirmVerified(userLocalDataSource, userRemoteDataSource)
        }

    @Test
    fun `GIVEN theme mode is 1 WHEN check theme THEN theme is light`() {
        every { userLocalDataSource.themeMode } returns 1
        mockkStatic(AppCompatDelegate::class)

        viewModel.checkTheme()

        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO) }
    }

    @Test
    fun `GIVEN theme mode is 2 WHEN check theme THEN theme is dark`() {
        every { userLocalDataSource.themeMode } returns 2
        mockkStatic(AppCompatDelegate::class)

        viewModel.checkTheme()

        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) }
    }

    @Test
    fun `GIVEN theme mode is other WHEN check theme THEN theme is system default`() {
        every { userLocalDataSource.themeMode } returns 3
        mockkStatic(AppCompatDelegate::class)

        viewModel.checkTheme()

        verify { AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM) }
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