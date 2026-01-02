/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _uiState: MutableState<LoginUiState> = mutableStateOf(
        LoginUiState.empty().copy(username = userRepository.username),
    )
    private val _loginError = MutableStateFlow<ErrorModel?>(null)
    private val _activityName = MutableStateFlow<String?>(null)
    //endregion

    //region Public properties
    val uiState: State<LoginUiState> = _uiState
    val loginError: StateFlow<ErrorModel?> = _loginError
    val activityName: StateFlow<String?> = _activityName
    //endregion

    //region Public methods
    fun login(username: String, password: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        userRepository.login(username, password).fold(
            onSuccess = {
                userRepository.loadConfig()
                booksRepository.loadBooks(userRepository.userId).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _activityName.value = MainActivity::class.simpleName
                    },
                    onFailure = {
                        userRepository.logout()
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _loginError.value = ErrorModel(
                            Constants.EMPTY_VALUE,
                            R.string.error_server,
                        )
                    },
                )
            },
            onFailure = {
                _uiState.value = _uiState.value.copy(isLoading = false)
                _loginError.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.wrong_credentials,
                )
            },
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

        _uiState.value = _uiState.value.copy(
            username = username,
            password = password,
            formState = LoginFormState(usernameError, passwordError, isDataValid),
        )
    }

    fun closeDialogs() {
        _loginError.value = null
    }
    //endregion
}