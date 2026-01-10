/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.presentation.register

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.data.remote.model.CustomExceptions
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.error_server
import reader_collection.app.generated.resources.error_user_found
import reader_collection.app.generated.resources.invalid_password
import reader_collection.app.generated.resources.invalid_repeat_password
import reader_collection.app.generated.resources.invalid_username

class RegisterViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _uiState: MutableState<RegisterUiState> = mutableStateOf(RegisterUiState.empty())
    private val _registerError = MutableStateFlow<ErrorModel?>(null)
    private val _infoDialogMessageId = MutableStateFlow<StringResource?>(null)
    private val _registerSuccess = MutableStateFlow(false)
    //endregion

    //region Public properties
    val uiState: State<RegisterUiState> = _uiState
    val registerError: StateFlow<ErrorModel?> = _registerError
    val infoDialogMessageId: StateFlow<StringResource?> = _infoDialogMessageId
    val registerSuccess: StateFlow<Boolean> = _registerSuccess
    //endregion

    //region Public methods
    fun register(username: String, password: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isLoading = true)
        userRepository.register(username, password).fold(
            onSuccess = {
                userRepository.login(username, password).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        _registerSuccess.value = true
                    },
                    onFailure = {
                        manageError(
                            ErrorModel(
                                Constants.EMPTY_VALUE,
                                Res.string.error_server,
                            ),
                        )
                    },
                )
            },
            onFailure = {
                manageError(
                    if (it is CustomExceptions.ExistentUser) {
                        ErrorModel(
                            Constants.EMPTY_VALUE,
                            Res.string.error_user_found,
                        )
                    } else {
                        ErrorModel(
                            Constants.EMPTY_VALUE,
                            Res.string.error_server,
                        )
                    },
                )
            },
        )
    }

    fun registerDataChanged(username: String, password: String, confirmPassword: String) {
        var usernameError: StringResource? = null
        var passwordError: StringResource? = null
        var isDataValid = true

        if (!Constants.isUserNameValid(username)) {
            usernameError = Res.string.invalid_username
            isDataValid = false
        }
        if (!Constants.isPasswordValid(password)) {
            passwordError = Res.string.invalid_password
            isDataValid = false
        }
        if (password != confirmPassword) {
            passwordError = Res.string.invalid_repeat_password
            isDataValid = false
        }

        _uiState.value = _uiState.value.copy(
            username = username,
            password = password,
            confirmPassword = confirmPassword,
            formState = LoginFormState(usernameError, passwordError, isDataValid),
        )
    }

    fun showInfoDialog(textId: StringResource) {
        _infoDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _registerError.value = null
        _infoDialogMessageId.value = null
    }
    //endregion

    //region Private methods
    private fun manageError(error: ErrorModel) {
        _uiState.value = _uiState.value.copy(isLoading = false)
        _registerError.value = error
    }
    //endregion
}