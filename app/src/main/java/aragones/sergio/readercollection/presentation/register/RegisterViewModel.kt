/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.presentation.register

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import com.aragones.sergio.util.Constants
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _uiState: MutableState<RegisterUiState> = mutableStateOf(RegisterUiState.empty())
    private val _registerError = MutableStateFlow<ErrorResponse?>(null)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _activityName = MutableStateFlow<String?>(null)
    //endregion

    //region Public properties
    val uiState: State<RegisterUiState> = _uiState
    val registerError: StateFlow<ErrorResponse?> = _registerError
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    val activityName: StateFlow<String?> = _activityName
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun register(username: String, password: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        userRepository
            .register(username, password)
            .subscribeBy(
                onComplete = {
                    userRepository
                        .login(username, password)
                        .subscribeBy(
                            onComplete = {
                                _uiState.value = _uiState.value.copy(isLoading = false)
                                _activityName.value = MainActivity::class.simpleName
                            },
                            onError = {
                                manageError(
                                    ErrorResponse(
                                        Constants.EMPTY_VALUE,
                                        R.string.error_server,
                                    ),
                                )
                            },
                        ).addTo(disposables)
                },
                onError = {
                    manageError(
                        if (it is FirebaseAuthUserCollisionException) {
                            ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_user_found,
                            )
                        } else {
                            ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_server,
                            )
                        },
                    )
                },
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

        _uiState.value = _uiState.value.copy(
            username = username,
            password = password,
            confirmPassword = confirmPassword,
            formState = LoginFormState(usernameError, passwordError, isDataValid),
        )
    }

    fun showInfoDialog(textId: Int) {
        _infoDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _registerError.value = null
        _infoDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun manageError(error: ErrorResponse) {
        _uiState.value = _uiState.value.copy(isLoading = false)
        _registerError.value = error
    }
    //endregion
}