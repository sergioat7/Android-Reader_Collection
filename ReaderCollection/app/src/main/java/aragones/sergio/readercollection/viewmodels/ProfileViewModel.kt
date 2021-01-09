/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {

    //MARK: - Private properties

    private val _userdata: MutableLiveData<UserData> = MutableLiveData(userRepository.userData)
    private val _profileForm = MutableLiveData<Int?>()
    private val _profileRedirection = MutableLiveData<Boolean>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val language: String = userRepository.language
    val sortParam: String? = userRepository.sortParam
    val profileUserData: LiveData<UserData> = _userdata
    val profileForm: LiveData<Int?> = _profileForm
    val profileRedirection: LiveData<Boolean> = _profileRedirection
    val profileLoading: LiveData<Boolean> = _profileLoading
    val profileError: LiveData<ErrorResponse> = _profileError

    //MARK: - Public methods

    fun logout() {

        _profileLoading.value = true
        userRepository.logout().subscribeBy(
            onComplete = {

                userRepository.removePassword()
                userRepository.removeCredentials()
                _profileLoading.value = false
                _profileRedirection.value = true
            },
            onError = {

                _profileLoading.value = false
                _profileError.value = Constants.handleError(it)
            }
        )
    }

    fun saveData(newPassword: String, newLanguage: String, newSortParam: String?) {

        val changePassword = newPassword != userRepository.userData.password
        val changeLanguage = newLanguage != language
        val changeSortParam = newSortParam != sortParam

        if (changePassword) {

            _profileLoading.value = true
            userRepository.updatePassword(newPassword).subscribeBy(
                onComplete = {

                    userRepository.storePassword(newPassword)
                    _profileLoading.value = false
                    _userdata.value = userRepository.userData
                    if (changeLanguage) {
                        _profileRedirection.value = true
                    }
                },
                onError = {

                    _profileLoading.value = false
                    _profileError.value = Constants.handleError(it)
                }
            )
        }

        if (changeSortParam) {
            userRepository.storeSortParam(newSortParam)
        }

        if (changeLanguage) {

            userRepository.storeLanguage(newLanguage)
            if (!changePassword) {
                _profileRedirection.value = true
            }
        }
    }

    fun login(username: String, password: String) {

        _profileLoading.value = true
        userRepository.login(username, password).subscribeBy(
            onSuccess = {

                val authData = AuthData(it.token)
                userRepository.storeCredentials(authData)
                _profileLoading.value = false
            },
            onError = {

                _profileLoading.value = false
                _profileError.value = Constants.handleError(it)
            }
        )
    }

    fun deleteUser() {

        _profileLoading.value = true
        userRepository.deleteUser().subscribeBy(
            onComplete = {

                userRepository.removeUserData()
                userRepository.removeCredentials()
                _profileLoading.value = false
                _profileRedirection.value = true
            },
            onError = {

                _profileLoading.value = false
                _profileError.value = Constants.handleError(it)
            }
        )
    }

    fun profileDataChanged(password: String) {

        var passwordError: Int? = null
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
        }
        _profileForm.value = passwordError
    }
}