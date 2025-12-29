/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private val userId: String
        get() = userRepository.userId
    private var _state: MutableState<DataSyncUiState> = mutableStateOf(
        DataSyncUiState.empty().copy(
            isAutomaticSyncEnabled = userRepository.isAutomaticSyncEnabled,
        ),
    )
    private val _error = MutableStateFlow<ErrorModel?>(null)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: State<DataSyncUiState> = _state
    val error: StateFlow<ErrorModel?> = _error
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    //endregion

    //region Public methods
    fun changeAutomaticSync(value: Boolean) {
        userRepository.storeAutomaticSync(value)
        _state.value = _state.value.copy(isAutomaticSyncEnabled = value)
    }
    fun syncData() = viewModelScope.launch {
        _state.value = _state.value.copy(isLoading = true)
        booksRepository.syncBooks(userId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.data_sync_successfully
                _state.value = _state.value.copy(isLoading = false)
            },
            onFailure = {
                _state.value = _state.value.copy(isLoading = false)
                _error.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_server,
                )
            },
        )
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
        _confirmationDialogMessageId.value = -1
        _error.value = null
    }
    //endregion
}