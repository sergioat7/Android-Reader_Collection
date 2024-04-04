/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.settings

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
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
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _password = MutableLiveData(userRepository.userData.password)
    private val _profileForm = MutableLiveData<Int?>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse?>()
    private val _language = MutableLiveData(userRepository.language)
    private val _sortParam = MutableLiveData(userRepository.sortParam)
    private val _isSortDescending = MutableLiveData(userRepository.isSortDescending)
    private val _themeMode = MutableLiveData(userRepository.themeMode)
    private val _activityName = MutableLiveData<String?>()
    private val _confirmationDialogMessageId = MutableLiveData(-1)
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    val username: String = userRepository.username
    val password: LiveData<String> = _password
    var language: LiveData<String> = _language
    var sortParam: LiveData<String?> = _sortParam
    var isSortDescending: LiveData<Boolean> = _isSortDescending
    var themeMode: LiveData<Int> = _themeMode
    val profileForm: LiveData<Int?> = _profileForm
    val profileLoading: LiveData<Boolean> = _profileLoading
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

        _password.value = userRepository.userData.password
        _profileForm.value = null
        _language.value = userRepository.language
        _sortParam.value = userRepository.sortParam
        _isSortDescending.value = userRepository.isSortDescending
        _themeMode.value = userRepository.themeMode
    }

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

    fun save() {

        val newPassword = requireNotNull(_password.value)
        val newLanguage = requireNotNull(_language.value)
        val newSortParam = _sortParam.value
        val newIsSortDescending = requireNotNull(_isSortDescending.value)
        val newThemeMode = requireNotNull(_themeMode.value)

        val changePassword = newPassword != userRepository.userData.password
        val changeLanguage = newLanguage != userRepository.language
        val changeSortParam = newSortParam != userRepository.sortParam
        val changeIsSortDescending = newIsSortDescending != userRepository.isSortDescending
        val changeThemeMode = newThemeMode != userRepository.themeMode

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
        }

        if (changeIsSortDescending) {
            userRepository.storeIsSortDescending(newIsSortDescending)
        }

        if (changeThemeMode) {

            userRepository.storeThemeMode(newThemeMode)
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

    fun profileDataChanged(
        newPassword: String,
        newLanguage: String,
        newSortParam: String?,
        newIsSortDescending: Boolean,
        newThemeMode: Int
    ) {

        var passwordError: Int? = null
        if (!Constants.isPasswordValid(newPassword)) {
            passwordError = R.string.invalid_password
        }
        _password.value = newPassword
        _profileForm.value = passwordError
        _language.value = newLanguage
        _sortParam.value = newSortParam
        _isSortDescending.value = newIsSortDescending
        _themeMode.value = newThemeMode
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