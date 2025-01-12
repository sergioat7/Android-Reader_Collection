/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.landing.LandingActivity
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _state: MutableState<SettingsUiState> = mutableStateOf(
        SettingsUiState.empty().copy(
            username = userRepository.username,
            password = userRepository.userData.password,
            language = userRepository.language,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
            themeMode = userRepository.themeMode,
        ),
    )
    private val _profileError = MutableLiveData<ErrorResponse?>()
    private val _activityName = MutableLiveData<String?>()
    private val _confirmationDialogMessageId = MutableLiveData(-1)
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    val state: State<SettingsUiState> = _state
    val profileError: LiveData<ErrorResponse?> = _profileError
    val activityName: LiveData<String?> = _activityName
    var tutorialShown = userRepository.hasSettingsTutorialBeenShown
    val confirmationDialogMessageId: LiveData<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val locale = AppCompatDelegate.getApplicationLocales().get(0) ?: Locale.getDefault()
            userRepository.language = locale.language
        }

        _state.value = _state.value.copy(
            password = userRepository.userData.password,
            passwordError = null,
            language = userRepository.language,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
            themeMode = userRepository.themeMode,
        )
    }

    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun logout() {
        userRepository.logout()
        _activityName.value = LandingActivity::class.simpleName
    }

    fun save() {
        val newPassword = requireNotNull(_state.value.password)
        val newLanguage = requireNotNull(_state.value.language)
        val newSortParam = _state.value.sortParam
        val newIsSortDescending = requireNotNull(_state.value.isSortDescending)
        val newThemeMode = requireNotNull(_state.value.themeMode)

        val changePassword = newPassword != userRepository.userData.password
        val changeLanguage = newLanguage != userRepository.language
        val changeSortParam = newSortParam != userRepository.sortParam
        val changeIsSortDescending = newIsSortDescending != userRepository.isSortDescending
        val changeThemeMode = newThemeMode != userRepository.themeMode

        if (changePassword) {
            _state.value = _state.value.copy(isLoading = true)
            userRepository
                .updatePassword(newPassword)
                .subscribeBy(
                    onComplete = {
                        _state.value = _state.value.copy(isLoading = false)
                        if (changeLanguage || changeSortParam || changeIsSortDescending) {
                            _activityName.value = LandingActivity::class.simpleName
                        }
                    },
                    onError = {
                        manageError(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
                    },
                ).addTo(disposables)
        }

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

        if (!changePassword && (changeLanguage || changeSortParam || changeIsSortDescending)) {
            _activityName.value = LandingActivity::class.simpleName
        }
    }

    fun deleteUser() {
        _state.value = _state.value.copy(isLoading = true)
        userRepository
            .deleteUser()
            .subscribeBy(
                onComplete = {
                    resetDatabase()
                },
                onError = {
                    manageError(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
                },
            ).addTo(disposables)
    }

    fun profileDataChanged(
        newPassword: String,
        newLanguage: String,
        newSortParam: String?,
        newIsSortDescending: Boolean,
        newThemeMode: Int,
    ) {
        var passwordError: Int? = null
        if (!Constants.isPasswordValid(newPassword)) {
            passwordError = R.string.invalid_password
        }
        _state.value = _state.value.copy(
            password = newPassword,
            passwordError = passwordError,
            language = newLanguage,
            sortParam = newSortParam,
            isSortDescending = newIsSortDescending,
            themeMode = newThemeMode,
        )
    }

    fun setTutorialAsShown() {
        userRepository.setHasSettingsTutorialBeenShown(true)
        tutorialShown = true
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun showInfoDialog(textId: Int) {
        _infoDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun resetDatabase() {
        booksRepository
            .resetTable()
            .subscribeBy(
                onComplete = {
                    _state.value = _state.value.copy(isLoading = false)
                    _activityName.value = LandingActivity::class.simpleName
                },
                onError = {
                    _state.value = _state.value.copy(isLoading = false)
                    _activityName.value = LandingActivity::class.simpleName
                },
            ).addTo(disposables)
    }

    private fun manageError(error: ErrorResponse) {
        _state.value = _state.value.copy(isLoading = false)
        _profileError.value = error
        _profileError.value = null
    }
    //endregion
}