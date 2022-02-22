/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.LoginFormState
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _registerForm = MutableLiveData<LoginFormState>()
    private val _registerLoading = MutableLiveData<Boolean>()
    private val _registerError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val registerFormState: LiveData<LoginFormState> = _registerForm
    val registerLoading: LiveData<Boolean> = _registerLoading
    val registerError: LiveData<ErrorResponse?> = _registerError
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun register(username: String, password: String) {

        _registerLoading.value = true
        userRepository.registerObserver(username, password).subscribeBy(
            onComplete = {
                login(username, password)
            },
            onError = {

                _registerLoading.value = false
                _registerError.value = ApiManager.handleError(it)
            }
        ).addTo(disposables)
    }

    fun registerDataChanged(username: String, password: String, confirmPassword: String) {

        var usernameError: Int? = null
        var passwordError: Int? = null
        var isDataValid = true

        if (!Constants.isUserNameValid(username)) {
            usernameError = R.string.invalid_username
            isDataValid = false
        }
        if (!Constants.isPasswordValid(password)) {
            passwordError = R.string.invalid_password
            isDataValid = false
        }
        if (password != confirmPassword) {
            passwordError = R.string.invalid_repeat_password
            isDataValid = false
        }
        _registerForm.value = LoginFormState(usernameError, passwordError, isDataValid)
    }
    //endregion

    //region Private methods
    private fun login(username: String, password: String) {

        userRepository.loginObserver(username, password).subscribeBy(
            onSuccess = {

                val userData = UserData(username, password, true)
                val authData = AuthData(it.token)
                userRepository.storeLoginData(userData, authData)
                _registerLoading.value = false
                _registerError.value = null
            },
            onError = {

                _registerLoading.value = false
                _registerError.value = ApiManager.handleError(it)
            }
        )
    }
    //endregion
}