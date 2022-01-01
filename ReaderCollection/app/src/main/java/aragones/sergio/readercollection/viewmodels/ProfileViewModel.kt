/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.viewmodels.base.BaseViewModel
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val formatRepository: FormatRepository,
    private val stateRepository: StateRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //MARK: - Private properties

    private val _profileForm = MutableLiveData<Int?>()
    private val _profileRedirection = MutableLiveData<Boolean>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val userData: UserData = userRepository.userData
    val language: String = userRepository.language
    var sortParam: String? = userRepository.sortParam
    var isSortDescending: Boolean = userRepository.isSortDescending
    var themeMode: Int = userRepository.themeMode
    val profileForm: LiveData<Int?> = _profileForm
    val profileRedirection: LiveData<Boolean> = _profileRedirection
    val profileLoading: LiveData<Boolean> = _profileLoading
    val profileError: LiveData<ErrorResponse> = _profileError

    // MARK: - Lifecycle methods

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        formatRepository.onDestroy()
        stateRepository.onDestroy()
    }

    //MARK: - Public methods

    fun logout() {

        _profileLoading.value = true

        userRepository.removePassword()
        userRepository.removeCredentials()
        userRepository.logoutObserver().subscribeBy(
            onComplete = {},
            onError = {}
        ).addTo(disposables)

        resetDatabase()
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
            userRepository.updatePasswordObserver(newPassword).subscribeBy(
                onComplete = {

                    userRepository.storePassword(newPassword)
                    loginObserver().subscribeBy(
                        onComplete = {

                            _profileLoading.value = false
                            if (changeLanguage) {
                                _profileRedirection.value = true
                            }
                        },
                        onError = {

                            _profileLoading.value = false
                            _profileError.value = Constants.handleError(it)
                            onDestroy()
                        }
                    ).addTo(disposables)
                },
                onError = {

                    _profileLoading.value = false
                    _profileError.value = Constants.handleError(it)
                    onDestroy()
                }
            ).addTo(disposables)
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

        if (changeLanguage) {

            userRepository.storeLanguage(newLanguage)
            if (!changePassword) {
                _profileRedirection.value = true
            }
        }
    }

    fun deleteUser() {

        _profileLoading.value = true
        userRepository.deleteUserObserver().subscribeBy(
            onComplete = {

                userRepository.removeUserData()
                userRepository.removeCredentials()
                resetDatabase()
            },
            onError = {

                _profileLoading.value = false
                _profileError.value = Constants.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun profileDataChanged(password: String) {

        var passwordError: Int? = null
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
        }
        _profileForm.value = passwordError
    }

    //MARK: - Private methods

    private fun resetDatabase() {

        var result = 0

        booksRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                result += 1
                checkProgress(result)
            }
        ).addTo(disposables)

        formatRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                result += 1
                checkProgress(result)
            }
        ).addTo(disposables)

        stateRepository.resetTableObserver().subscribeBy(
            onComplete = {

                result += 1
                checkProgress(result)
            },
            onError = {

                result += 1
                checkProgress(result)
            }
        ).addTo(disposables)
    }

    private fun checkProgress(result: Int) {

        if (result == 3) {

            _profileLoading.value = false
            _profileRedirection.value = true
        }
    }

    private fun loginObserver(): Completable {

        return Completable.create { emitter ->

            val username = userRepository.username
            val password = userRepository.userData.password
            userRepository.loginObserver(username, password).subscribeBy(
                onSuccess = {

                    val authData = AuthData(it.token)
                    userRepository.storeCredentials(authData)
                    emitter.onComplete()
                },
                onError = {
                    emitter.onError(it)
                }
            ).addTo(disposables)
        }
            .subscribeOn(Constants.SUBSCRIBER_SCHEDULER)
            .observeOn(Constants.OBSERVER_SCHEDULER)
    }
}