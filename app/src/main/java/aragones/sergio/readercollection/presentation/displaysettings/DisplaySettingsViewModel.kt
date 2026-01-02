/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.displaysettings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.domain.UserRepository
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DisplaySettingsViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableState<DisplaySettingsUiState> = mutableStateOf(
        DisplaySettingsUiState.empty().copy(
            language = userRepository.language,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
            themeMode = userRepository.themeMode,
        ),
    )
    private val _relaunch = MutableStateFlow(false)
    //endregion

    //region Public properties
    val state: State<DisplaySettingsUiState> = _state
    val relaunch: StateFlow<Boolean> = _relaunch
    //endregion

    //region Lifecycle methods
    fun onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            userRepository.language = locale.language
        }

        _state.value = _state.value.copy(
            language = userRepository.language,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
            themeMode = userRepository.themeMode,
        )
    }
    //endregion

    //region Public methods
    fun save() {
        val newLanguage = requireNotNull(_state.value.language)
        val newSortParam = _state.value.sortParam
        val newIsSortDescending = requireNotNull(_state.value.isSortDescending)
        val newThemeMode = requireNotNull(_state.value.themeMode)

        val changeLanguage = newLanguage != userRepository.language
        val changeSortParam = newSortParam != userRepository.sortParam
        val changeIsSortDescending = newIsSortDescending != userRepository.isSortDescending
        val changeThemeMode = newThemeMode != userRepository.themeMode

        if (changeLanguage) {
            userRepository.storeLanguage(newLanguage)
        }

        if (changeSortParam) {
            userRepository.storeSortParam(newSortParam)
        }

        if (changeIsSortDescending) {
            userRepository.storeIsSortDescending(newIsSortDescending)
        }

        if (changeThemeMode) {
            userRepository.storeThemeMode(newThemeMode)
            when (newThemeMode) {
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                )
            }
        }

        if (changeSortParam || changeIsSortDescending || changeThemeMode) {
            _relaunch.value = true
        }
    }

    fun profileDataChanged(
        newLanguage: String,
        newSortParam: String?,
        newIsSortDescending: Boolean,
        newThemeMode: Int,
    ) {
        _state.value = _state.value.copy(
            language = newLanguage,
            sortParam = newSortParam,
            isSortDescending = newIsSortDescending,
            themeMode = newThemeMode,
        )
    }
    //endregion
}