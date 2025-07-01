/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

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
class DataSyncViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val userId: String
        get() = userRepository.userId
    private var _state: MutableState<DataSyncUiState> = mutableStateOf(
        DataSyncUiState.empty().copy(
            isAutomaticSyncEnabled = true,
        ),
    )
    private val _error = MutableStateFlow<ErrorResponse?>(null)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: State<DataSyncUiState> = _state
    val error: StateFlow<ErrorResponse?> = _error
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun changeAutomaticSync(value: Boolean) {
        _state.value = _state.value.copy(isAutomaticSyncEnabled = value)
    }
    fun syncData() {
        _state.value = _state.value.copy(isLoading = true)
        booksRepository
            .syncBooks(userId)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.data_sync_successfully
                    _state.value = _state.value.copy(isLoading = false)
                },
                onError = {
                    _state.value = _state.value.copy(isLoading = false)
                    _error.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_server,
                    )
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
        _error.value = null
    }
    //endregion
}