/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.navigation.Route
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.FriendDetail>()
    private var _state: MutableStateFlow<FriendDetailUiState> =
        MutableStateFlow(FriendDetailUiState.Loading)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _error = MutableStateFlow<ErrorResponse?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<FriendDetailUiState> = _state
    var confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    val error: StateFlow<ErrorResponse?> = _error
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchFriend() {
        userRepository
            .getFriend(params.userId)
            .subscribeBy(
                onSuccess = { friend ->
                    booksRepository
                        .getBooksFrom(params.userId)
                        .onErrorReturnItem(emptyList())
                        .subscribeBy(
                            onSuccess = { books ->
                                _state.value = FriendDetailUiState.Success(friend, books)
                            },
                        ).addTo(disposables)
                },
                onError = {
                    when (it) {
                        is NoSuchElementException -> {
                            _error.value = ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.no_friends_found,
                            )
                        }
                        else -> {
                            _error.value = ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_server,
                            )
                        }
                    }
                },
            ).addTo(disposables)
    }

    fun deleteFriend() {
        val currentState = _state.value
        _state.value = FriendDetailUiState.Loading
        userRepository
            .deleteFriend(params.userId)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.friend_removed
                },
                onError = {
                    _error.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.error_search,
                    )
                    _state.value = currentState
                },
            ).addTo(disposables)
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
        _error.value = null
    }
    //endregion
}