/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.ui.register.RegisterActivity
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private var _uiState: MutableState<LoginUiState> = mutableStateOf(
        LoginUiState.empty().copy(username = userRepository.username)
    )
    private val _loginError = MutableLiveData<ErrorResponse?>()
    private val _activityName = MutableLiveData<String?>()
    //endregion

    //region Public properties
    val uiState: State<LoginUiState> = _uiState
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

        _uiState.value = _uiState.value.copy(isLoading = true)
        userRepository.login(username, password, success = {
//            booksRepository.loadBooks(success = {

            _uiState.value = _uiState.value.copy(isLoading = false)
            _activityName.value = MainActivity::class.simpleName
            _activityName.value = null
//            }, failure = {
//
//                _loginLoading.value = false
//                _loginError.value = it
//            })
        }, failure = {

            _uiState.value = _uiState.value.copy(isLoading = false)
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

        _uiState.value = _uiState.value.copy(
            username = username,
            password = password,
            formState = LoginFormState(usernameError, passwordError, isDataValid),
        )
    }

    fun goToRegister() {

        _activityName.value = RegisterActivity::class.simpleName
        _activityName.value = null
    }

    fun closeDialogs() {
        _loginError.value = null
    }
    //endregion
}