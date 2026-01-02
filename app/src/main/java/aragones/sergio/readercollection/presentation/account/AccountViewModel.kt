/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AccountViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<AccountUiState> = MutableStateFlow(
        AccountUiState.empty().copy(
            username = userRepository.username,
            password = userRepository.userData.password,
        ),
    )
    private val _profileError = MutableStateFlow<ErrorModel?>(null)
    private val _logOut = MutableStateFlow(false)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: StateFlow<AccountUiState> = _state
    val profileError: StateFlow<ErrorModel?> = _profileError
    val logOut: StateFlow<Boolean> = _logOut
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onResume() {
        _state.update {
            it.copy(
                password = userRepository.userData.password,
                passwordError = null,
                isProfilePublic = userRepository.isProfilePublic,
            )
        }
    }
    //endregion

    //region Public methods
    fun save() {
        val newPassword = requireNotNull(_state.value.password)

        if (newPassword != userRepository.userData.password) {
            _state.update { it.copy(isLoading = true) }
            viewModelScope.launch {
                userRepository.updatePassword(newPassword).fold(
                    onSuccess = {
                        _state.update { it.copy(isLoading = false) }
                    },
                    onFailure = {
                        manageError(ErrorModel(Constants.EMPTY_VALUE, R.string.error_server))
                    },
                )
            }
        }
    }

    fun setPublicProfile(value: Boolean) = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        userRepository.setPublicProfile(value).fold(
            onSuccess = {
                _state.update {
                    it.copy(
                        isProfilePublic = value,
                        isLoading = false,
                    )
                }
            },
            onFailure = {
                manageError(ErrorModel(Constants.EMPTY_VALUE, R.string.error_server))
            },
        )
    }

    fun deleteUser() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        userRepository.deleteUser().fold(
            onSuccess = {
                resetDatabase()
            },
            onFailure = {
                manageError(ErrorModel(Constants.EMPTY_VALUE, R.string.error_server))
            },
        )
    }

    fun profileDataChanged(newPassword: String) {
        var passwordError: Int? = null
        if (!Constants.isPasswordValid(newPassword)) {
            passwordError = R.string.invalid_password
        }
        _state.update {
            it.copy(
                password = newPassword,
                passwordError = passwordError,
            )
        }
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun showInfoDialog(textId: Int) {
        _infoDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
        _profileError.value = null
    }
    //endregion

    //region Private methods
    private suspend fun resetDatabase() {
        booksRepository.resetTable().fold(
            onSuccess = {
                _state.update { it.copy(isLoading = false) }
                _logOut.value = true
            },
            onFailure = {
                _state.update { it.copy(isLoading = false) }
                _logOut.value = true
            },
        )
    }

    private fun manageError(error: ErrorModel) {
        _state.update { it.copy(isLoading = false) }
        _profileError.value = error
    }
    //endregion
}