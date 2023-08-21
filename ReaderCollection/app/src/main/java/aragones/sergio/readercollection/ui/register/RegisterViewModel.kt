/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.source.UserRepository
import aragones.sergio.readercollection.models.AuthData
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.models.LoginFormState
import aragones.sergio.readercollection.models.UserData
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.utils.Constants
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
        userRepository.register(username, password, success = {
            userRepository.login(username, password, success = { token ->

                val userData = UserData(username, password, true)
                val authData = AuthData(token)
                userRepository.storeLoginData(userData, authData)
                _registerLoading.value = false
                _registerError.value = null
            }, failure = {

                _registerLoading.value = false
                _registerError.value = it
            })
        }, failure = {

            _registerLoading.value = false
            _registerError.value = it
        })
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
}