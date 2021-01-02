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
import aragones.sergio.readercollection.repositories.ProfileRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
): ViewModel() {

    //MARK: - Private properties

    private val _userdata: MutableLiveData<UserData> = MutableLiveData(profileRepository.userData)
    private val _profileForm = MutableLiveData<Int?>()
    private val _profileRedirection = MutableLiveData<Boolean>()
    private val _profileLoading = MutableLiveData<Boolean>()
    private val _profileError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val language: String = profileRepository.language
    val sortParam: String? = profileRepository.sortParam
    val profileUserData: LiveData<UserData> = _userdata
    val profileForm: LiveData<Int?> = _profileForm
    val profileRedirection: LiveData<Boolean> = _profileRedirection
    val profileLoading: LiveData<Boolean> = _profileLoading
    val profileError: LiveData<ErrorResponse> = _profileError

    //MARK: - Public methods

    fun logout() {

        _profileLoading.value = true
        profileRepository.logout().subscribeBy(
            onComplete = {

                profileRepository.removePassword()
                profileRepository.removeCredentials()
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

        val changePassword = newPassword != profileRepository.userData.password
        val changeLanguage = newLanguage != language
        val changeSortParam = newSortParam != sortParam

        if (changePassword) {

            _profileLoading.value = true
            profileRepository.updatePassword(newPassword).subscribeBy(
                onComplete = {

                    profileRepository.storePassword(newPassword)
                    _profileLoading.value = false
                    _userdata.value = profileRepository.userData
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
            profileRepository.storeSortParam(newSortParam)
        }

        if (changeLanguage) {

            profileRepository.storeLanguage(newLanguage)
            if (!changePassword) {
                _profileRedirection.value = true
            }
        }
    }

    fun login(username: String, password: String) {

        _profileLoading.value = true
        profileRepository.login(username, password).subscribeBy(
            onSuccess = {

                val authData = AuthData(it.token)
                profileRepository.storeCredentials(authData)
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
        profileRepository.deleteUser().subscribeBy(
            onComplete = {

                profileRepository.removeUserData()
                profileRepository.removeCredentials()
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