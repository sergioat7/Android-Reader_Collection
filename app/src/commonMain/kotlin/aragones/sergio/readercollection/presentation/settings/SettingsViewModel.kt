/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

class SettingsViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private val _state: MutableStateFlow<SettingsUiState> =
        MutableStateFlow(SettingsUiState("", false))
    private val _logOut = MutableStateFlow(false)
    private val _confirmationDialogMessageId = MutableStateFlow<StringResource?>(null)
    //endregion

    //region Public properties
    var state: StateFlow<SettingsUiState> = _state
    val logOut: StateFlow<Boolean> = _logOut
    val confirmationDialogMessageId: StateFlow<StringResource?> = _confirmationDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onResume() {
        _state.update {
            it.copy(version = userRepository.getAppVersion())
        }
    }
    //endregion

    //region Public methods
    fun logout() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }
        userRepository.logout()
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

    fun showConfirmationDialog(textId: StringResource) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = null
    }
    //endregion
}