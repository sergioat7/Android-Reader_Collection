/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _profileForm = MutableLiveData<Int?>()
    private val _profileRedirection = MutableLiveData<Boolean>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val userData: UserData = userRepository.userData
    val language: String = userRepository.language
    var sortParam: String? = userRepository.sortParam
    var isSortDescending: Boolean = userRepository.isSortDescending
    var themeMode: Int = userRepository.themeMode
    val profileForm: LiveData<Int?> = _profileForm
    val profileRedirection: LiveData<Boolean> = _profileRedirection
    val profileLoading: LiveData<Boolean> = _profileLoading
    val profileError: LiveData<ErrorResponse?> = _profileError
    var tutorialShown = userRepository.hasSettingsTutorialBeenShown
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
//        userRepository.removePassword()
//        userRepository.removeCredentials()
        userRepository.logout()
//        resetDatabase()

        _profileRedirection.value = true
    }

    fun save(
        newPassword: String,
        newLanguage: String,
        newSortParam: String?,
        newIsSortDescending: Boolean,
        newThemeMode: Int
    ) {

        val changePassword =            newPassword != userRepository.userData.password
        val changeLanguage =            newLanguage != language
        val changeSortParam =           newSortParam != sortParam
        val changeIsSortDescending =    newIsSortDescending != isSortDescending
        val changeThemeMode =           newThemeMode != themeMode

        if (changePassword) {

            _profileLoading.value = true
            userRepository.updatePassword(newPassword, success = {

                userRepository.storePassword(newPassword)
                userRepository.login(userRepository.username, newPassword, success = { token ->

                    userRepository.storeCredentials(AuthData(token))
                    _profileLoading.value = false
                    if (changeLanguage || changeSortParam || changeIsSortDescending) {
                        _profileRedirection.value = true
                    }
                }, failure = {
                    manageError(it)
                })
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
            _profileRedirection.value = true
        }
    }

    fun deleteUser() {

        _profileLoading.value = true
        userRepository.deleteUser(success = {

            userRepository.removeUserData()
            userRepository.removeCredentials()
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
    //endregion

    //region Private methods
    private fun resetDatabase() {

        booksRepository.resetTableObserver().subscribeBy(
            onComplete = {

                _profileLoading.value = false
                _profileRedirection.value = true
            },
            onError = {

                _profileLoading.value = false
                _profileRedirection.value = true
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