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
import aragones.sergio.readercollection.ui.MainActivity
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.ui.register.RegisterActivity
import com.aragones.sergio.data.auth.AuthData
import com.aragones.sergio.data.auth.LoginFormState
import com.aragones.sergio.data.auth.UserData
import com.aragones.sergio.data.business.ErrorResponse
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _username = MutableLiveData(userRepository.username)
    private val _password = MutableLiveData<String>()
    private val _loginForm = MutableLiveData<LoginFormState>()
    private val _loginLoading = MutableLiveData<Boolean>()
    private val _loginError = MutableLiveData<ErrorResponse?>()
    private val _activityName = MutableLiveData<String?>()
    //endregion

    //region Public properties
    val username: LiveData<String> = _username
    val password: LiveData<String> = _password
    val loginFormState: LiveData<LoginFormState> = _loginForm
    val loginLoading: LiveData<Boolean> = _loginLoading
    val loginError: LiveData<ErrorResponse?> = _loginError
    val activityName: LiveData<String?> = _activityName
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
            _activityName.value = MainActivity::class.simpleName
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

        _username.value = username
        _password.value = password
        _loginForm.value = LoginFormState(usernameError, passwordError, isDataValid)
    }

    fun goToRegister() {
        _activityName.value = RegisterActivity::class.simpleName
    }
    //endregion
}