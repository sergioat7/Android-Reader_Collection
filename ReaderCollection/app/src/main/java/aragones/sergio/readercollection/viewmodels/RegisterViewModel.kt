/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
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
import aragones.sergio.readercollection.repositories.RegisterRepository
import aragones.sergio.readercollection.utils.Constants
import io.reactivex.rxjava3.kotlin.subscribeBy
import retrofit2.HttpException
import javax.inject.Inject

class RegisterViewModel @Inject constructor(
        private val registerRepository: RegisterRepository
): ViewModel() {

        //MARK: - Private properties

        private val _registerForm = MutableLiveData<LoginFormState>()
        private val _registerLoading = MutableLiveData<Boolean>()
        private val _registerError = MutableLiveData<ErrorResponse>()
        private val _loginError = MutableLiveData<ErrorResponse>()

        //MARK: - Public properties

        val registerFormState: LiveData<LoginFormState> = _registerForm
        val registerLoading: LiveData<Boolean> = _registerLoading
        val registerError: LiveData<ErrorResponse> = _registerError
        val loginError: LiveData<ErrorResponse> = _loginError

        //MARK: - Public methods

        fun register(username: String, password: String) {

                _registerLoading.value = true
                registerRepository.register(username, password).subscribeBy(
                        onComplete = {

                                _registerLoading.value = false
                                _registerError.value = null
                        },
                        onError = { error ->

                                _registerLoading.value = false
                                if (error is HttpException) {
                                        error.response()?.errorBody()?.let { errorBody ->

                                                _registerError.value = APIClient.gson.fromJson(
                                                        errorBody.charStream(), ErrorResponse::class.java
                                                )
                                        } ?: run {
                                                _registerError.value = ErrorResponse("", R.string.login_failed)
                                        }
                                } else {
                                        _registerError.value = ErrorResponse("", R.string.login_failed)
                                }
                        }
                )
        }

        fun login(username: String, password: String) {

                _registerLoading.value = true
                registerRepository.login(username, password).subscribeBy(
                        onSuccess = {

                                _registerLoading.value = false
                                val userData = UserData(username, password, true)
                                val authData = AuthData(it.token)
                                registerRepository.storeLoginData(userData, authData)
                                _loginError.value = null
                        },
                        onError = { error ->

                                _registerLoading.value = false
                                if (error is HttpException) {
                                        error.response()?.errorBody()?.let { errorBody ->

                                                _loginError.value = APIClient.gson.fromJson(
                                                        errorBody.charStream(), ErrorResponse::class.java
                                                )
                                        } ?: run {
                                                _loginError.value = ErrorResponse("", R.string.login_failed)
                                        }
                                } else {
                                        _loginError.value = ErrorResponse("", R.string.login_failed)
                                }
                        }
                )
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
}