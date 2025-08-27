/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _uiState: MutableState<LoginUiState> = mutableStateOf(
        LoginUiState.empty().copy(username = userRepository.username),
    )
    private val _loginError = MutableStateFlow<ErrorResponse?>(null)
    private val _activityName = MutableStateFlow<String?>(null)
    //endregion

    //region Public properties
    val uiState: State<LoginUiState> = _uiState
    val loginError: StateFlow<ErrorResponse?> = _loginError
    val activityName: StateFlow<String?> = _activityName
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun login(username: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        userRepository
            .login(username, password)
            .subscribeBy(
                onComplete = {
                    userRepository
                        .loadConfig()
                        .subscribeBy(
                            onComplete = {
                                val userId = userRepository.userId
                                booksRepository
                                    .loadBooks(userId)
                                    .subscribeBy(
                                        onComplete = {
                                            _uiState.value = _uiState.value.copy(isLoading = false)
                                            _activityName.value = MainActivity::class.simpleName
                                        },
                                        onError = {
                                            userRepository.logout()
                                            _uiState.value = _uiState.value.copy(isLoading = false)
                                            _loginError.value = ErrorResponse(
                                                Constants.EMPTY_VALUE,
                                                R.string.error_server,
                                            )
                                        },
                                    ).addTo(disposables)
                            },
                            onError = {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                _loginError.value = ErrorResponse(
                                    Constants.EMPTY_VALUE,
                                    R.string.wrong_credentials,
                                )
                            },
                        ).addTo(disposables)
                },
                onError = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    _loginError.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.wrong_credentials,
                    )
                },
            ).addTo(disposables)
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