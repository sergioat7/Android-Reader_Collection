/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.UserRepository
import aragones.sergio.readercollection.models.AuthData
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.models.LoginFormState
import aragones.sergio.readercollection.models.UserData
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.utils.Constants
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _loginForm = MutableLiveData<LoginFormState>()
    private val _loginLoading = MutableLiveData<Boolean>()
    private val _loginError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val username: String = userRepository.username
    val loginFormState: LiveData<LoginFormState> = _loginForm
    val loginLoading: LiveData<Boolean> = _loginLoading
    val loginError: LiveData<ErrorResponse?> = _loginError
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun login(username: String, password: String) {

        _loginLoading.value = true
        userRepository.login(username, password, success = { token ->

            val userData = UserData(username, password, true)
            val authData = AuthData(token)
            userRepository.storeLoginData(userData, authData)
//            booksRepository.loadBooks(success = {

                _loginLoading.value = false
                _loginError.value = null
//            }, failure = {
//
//                _loginLoading.value = false
//                _loginError.value = it
//            })
        }, failure = {

            _loginLoading.value = false
            _loginError.value = it
        })
    }

    fun loginDataChanged(username: String, password: String) {

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
        _loginForm.value = LoginFormState(usernameError, passwordError, isDataValid)
    }
    //endregion
}