/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/10/2025
 */

@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("ktlint:standard:max-line-length")

package aragones.sergio.readercollection.presentation

import app.cash.turbine.test
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.displaysettings.DisplaySettingsUiState
import aragones.sergio.readercollection.presentation.displaysettings.DisplaySettingsViewModel
import io.mockk.Runs
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test

class DisplaySettingsViewModelTest {

    private val userLocalDataSource: UserLocalDataSource = mockk {
        every { language } returns "en"
        every { sortParam } returns "sortParam"
        every { isSortDescending } returns true
        every { themeMode } returns 0
    }
    private val userRemoteDataSource: UserRemoteDataSource = mockk()
    private val ioDispatcher = UnconfinedTestDispatcher()
    private val viewModel = DisplaySettingsViewModel(
        UserRepository(
            userLocalDataSource,
            userRemoteDataSource,
            ioDispatcher,
        ),
    )

    @Test
    fun `GIVEN changes WHEN save THEN store changes and relaunch`() = runTest {
        val newLanguage = "es"
        val newSortParam = "newSortParam"
        val newIsSortDescending = false
        val newThemeMode = 1
        every { userLocalDataSource.storeLanguage(any()) } just Runs
        every { userLocalDataSource.storeSortParam(any()) } just Runs
        every { userLocalDataSource.storeIsSortDescending(any()) } just Runs
        every { userLocalDataSource.storeThemeMode(any()) } just Runs
        viewModel.profileDataChanged(newLanguage, newSortParam, newIsSortDescending, newThemeMode)

        viewModel.relaunch.test {
            Assert.assertEquals(false, awaitItem())

            viewModel.save()

            Assert.assertEquals(true, awaitItem())
        }

        verify(exactly = 2) { userLocalDataSource.language }
        verify(exactly = 2) { userLocalDataSource.sortParam }
        verify(exactly = 2) { userLocalDataSource.isSortDescending }
        verify(exactly = 2) { userLocalDataSource.themeMode }
        verify { userLocalDataSource.storeLanguage(newLanguage) }
        verify { userLocalDataSource.storeSortParam(newSortParam) }
        verify { userLocalDataSource.storeIsSortDescending(newIsSortDescending) }
        verify { userLocalDataSource.storeThemeMode(newThemeMode) }
        confirmVerified(userLocalDataSource)
    }

    @Test
    fun `GIVEN change just language WHEN save THEN store change and not relaunch`() = runTest {
        val newLanguage = "es"
        every { userLocalDataSource.storeLanguage(any()) } just Runs
        viewModel.profileDataChanged(newLanguage, "sortParam", true, 0)

        viewModel.relaunch.test {
            Assert.assertEquals(false, awaitItem())

            viewModel.save()

            expectNoEvents()
        }

        verify(exactly = 2) { userLocalDataSource.language }
        verify(exactly = 2) { userLocalDataSource.sortParam }
        verify(exactly = 2) { userLocalDataSource.isSortDescending }
        verify(exactly = 2) { userLocalDataSource.themeMode }
        verify { userLocalDataSource.storeLanguage(newLanguage) }
        verify(exactly = 0) { userLocalDataSource.storeSortParam(any()) }
        verify(exactly = 0) { userLocalDataSource.storeIsSortDescending(any()) }
        verify(exactly = 0) { userLocalDataSource.storeThemeMode(any()) }
        confirmVerified(userLocalDataSource)
    }

    @Test
    fun `GIVEN new values WHEN profileDataChanged THEN update state with new values`() {
        val newLanguage = "es"
        val newSortParam = "newSortParam"
        val newIsSortDescending = false
        val newThemeMode = 1

        Assert.assertEquals(
            DisplaySettingsUiState.empty().copy(
                language = "en",
                sortParam = "sortParam",
                isSortDescending = true,
                themeMode = 0,
            ),
            viewModel.state.value,
        )

        viewModel.profileDataChanged(newLanguage, newSortParam, newIsSortDescending, newThemeMode)

        Assert.assertEquals(
            DisplaySettingsUiState.empty().copy(
                language = newLanguage,
                sortParam = newSortParam,
                isSortDescending = newIsSortDescending,
                themeMode = newThemeMode,
            ),
            viewModel.state.value,
        )
    }
}