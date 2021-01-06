/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.LoginFormState
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.apiclient.APIClient
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.LoginRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.HttpException
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val formatRepository: FormatRepository,
): ViewModel() {

    //MARK: - Private properties

    private val _loginForm = MutableLiveData<LoginFormState>()
    private val _loginLoading = MutableLiveData<Boolean>()
    private val _loginError = MutableLiveData<ErrorResponse>()

    //MARK: - Public properties

    val username: String? = loginRepository.username
    val loginFormState: LiveData<LoginFormState> = _loginForm
    val loginLoading: LiveData<Boolean> = _loginLoading
    val loginError: LiveData<ErrorResponse> = _loginError

    //MARK: - Public methods

    fun login(username: String, password: String) {

        _loginLoading.value = true
        loginRepository.login(username, password).subscribeBy(
            onSuccess = {

                _loginLoading.value = false
                val userData = UserData(username, password, true)
                val authData = AuthData(it.token)
                loginRepository.storeLoginData(userData, authData)
                _loginError.value = null
            },
            onError = {

                _loginLoading.value = false
                _loginError.value = Constants.handleError(it)
            }
        )
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
}