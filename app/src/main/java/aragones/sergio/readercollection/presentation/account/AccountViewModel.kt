/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.account

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _state: MutableState<AccountUiState> = mutableStateOf(
        AccountUiState.empty().copy(
            username = userRepository.username,
            password = userRepository.userData.password,
        ),
    )
    private val _profileError = MutableStateFlow<ErrorResponse?>(null)
    private val _logOut = MutableStateFlow(false)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: State<AccountUiState> = _state
    val profileError: StateFlow<ErrorResponse?> = _profileError
    val logOut: StateFlow<Boolean> = _logOut
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onResume() {
        _state.value = _state.value.copy(
            password = userRepository.userData.password,
            passwordError = null,
        )
    }

    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun save() {
        val newPassword = requireNotNull(_state.value.password)

        if (newPassword != userRepository.userData.password) {
            _state.value = _state.value.copy(isLoading = true)
            userRepository
                .updatePassword(newPassword)
                .subscribeBy(
                    onComplete = {
                        _state.value = _state.value.copy(isLoading = false)
                    },
                    onError = {
                        manageError(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
                    },
                ).addTo(disposables)
        }
    }

    fun setPublicProfile(value: Boolean) {
        _state.value = _state.value.copy(
            isProfilePublic = value,
        )
    }

    fun deleteUser() {
        _state.value = _state.value.copy(isLoading = true)
        userRepository
            .deleteUser()
            .subscribeBy(
                onComplete = {
                    resetDatabase()
                },
                onError = {
                    manageError(ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server))
                },
            ).addTo(disposables)
    }

    fun profileDataChanged(newPassword: String) {
        var passwordError: Int? = null
        if (!Constants.isPasswordValid(newPassword)) {
            passwordError = R.string.invalid_password
        }
        _state.value = _state.value.copy(
            password = newPassword,
            passwordError = passwordError,
        )
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
    }
    //endregion

    //region Private methods
    private fun resetDatabase() {
        booksRepository
            .resetTable()
            .subscribeBy(
                onComplete = {
                    _state.value = _state.value.copy(isLoading = false)
                    _logOut.value = true
                },
                onError = {
                    _state.value = _state.value.copy(isLoading = false)
                    _logOut.value = true
                },
            ).addTo(disposables)
    }

    private fun manageError(error: ErrorResponse) {
        _state.value = _state.value.copy(isLoading = false)
        _profileError.value = error
        _profileError.value = null
    }
    //endregion
}