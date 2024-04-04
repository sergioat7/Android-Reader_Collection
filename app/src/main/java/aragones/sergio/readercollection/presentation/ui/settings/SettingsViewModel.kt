/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _profileForm = MutableLiveData<Int?>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse?>()
    private val _activityName = MutableLiveData<String?>()
    private val _confirmationDialogMessageId = MutableLiveData(-1)
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    val userData: UserData = userRepository.userData
    var language: String
        get() = userRepository.language
        set(value) {
            userRepository.storeLanguage(value)
        }
    var sortParam: String? = userRepository.sortParam
    var isSortDescending: Boolean = userRepository.isSortDescending
    var themeMode: Int = userRepository.themeMode
    val profileForm: LiveData<Int?> = _profileForm
    val profileLoading: LiveData<Boolean> = _profileLoading
    val profileError: LiveData<ErrorResponse?> = _profileError
    val activityName: LiveData<String?> = _activityName
    var tutorialShown = userRepository.hasSettingsTutorialBeenShown
    val confirmationDialogMessageId: LiveData<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun logout() {

//        _profileLoading.value = true
        userRepository.logout()
//        resetDatabase()

        _activityName.value = LandingActivity::class.simpleName
    }

    fun save(
        newPassword: String,
        newLanguage: String,
        newSortParam: String?,
        newIsSortDescending: Boolean,
        newThemeMode: Int
    ) {

        val changePassword = newPassword != userRepository.userData.password
        val changeLanguage = newLanguage != language
        val changeSortParam = newSortParam != sortParam
        val changeIsSortDescending = newIsSortDescending != isSortDescending
        val changeThemeMode = newThemeMode != themeMode

        if (changePassword) {

            _profileLoading.value = true
            userRepository.updatePassword(newPassword, success = {

                _profileLoading.value = false
                if (changeLanguage || changeSortParam || changeIsSortDescending) {
                    _activityName.value = LandingActivity::class.simpleName
                }
            }, failure = {
                manageError(it)
            })
        }

        if (changeLanguage) {
            userRepository.storeLanguage(newLanguage)
        }

        if (changeSortParam) {
            userRepository.storeSortParam(newSortParam)
            sortParam = newSortParam
        }

        if (changeIsSortDescending) {
            userRepository.storeIsSortDescending(newIsSortDescending)
            isSortDescending = newIsSortDescending
        }

        if (changeThemeMode) {

            userRepository.storeThemeMode(newThemeMode)
            themeMode = newThemeMode
            when (newThemeMode) {
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        if (!changePassword && (changeLanguage || changeSortParam || changeIsSortDescending)) {
            _activityName.value = LandingActivity::class.simpleName
        }
    }

    fun deleteUser() {

        _profileLoading.value = true
        userRepository.deleteUser(success = {
            resetDatabase()
        }, failure = {
            manageError(it)
        })
    }

    fun profileDataChanged(password: String) {

        var passwordError: Int? = null
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
        }
        _profileForm.value = passwordError
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

        booksRepository.resetTable().subscribeBy(
            onComplete = {

                _profileLoading.value = false
                _activityName.value = LandingActivity::class.simpleName
            },
            onError = {

                _profileLoading.value = false
                _activityName.value = LandingActivity::class.simpleName
            }
        ).addTo(disposables)
    }

    private fun manageError(error: ErrorResponse) {

        _profileLoading.value = false
        _profileError.value = error
        _profileError.value = null
    }
    //endregion
}