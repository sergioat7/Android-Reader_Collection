/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.navigation.Route
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FriendDetailViewModel(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.FriendDetail>()
    private var _state: MutableStateFlow<FriendDetailUiState> =
        MutableStateFlow(FriendDetailUiState.Loading)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    private val _error = MutableStateFlow<ErrorModel?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<FriendDetailUiState> = _state
    var confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    val error: StateFlow<ErrorModel?> = _error
    //endregion

    //region Public methods
    fun fetchFriend() = viewModelScope.launch {
        val friendRequest = async { userRepository.getFriend(params.userId) }
        val booksRequest = async { booksRepository.getBooksFrom(params.userId) }

        val friendResult = friendRequest.await()
        val booksResult = booksRequest.await()

        val friend = friendResult.getOrNull()
        val books = booksResult.getOrNull()

        if (friend != null && books != null) {
            _state.value = FriendDetailUiState.Success(
                friend = friend,
                books = Books(books),
            )
        } else {
            val error = friendResult.exceptionOrNull() ?: booksResult.exceptionOrNull()
            when (error) {
                is NoSuchElementException -> {
                    _error.value = ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.no_friends_found,
                    )
                }
                else -> {
                    _error.value = ErrorModel(
                        Constants.EMPTY_VALUE,
                        R.string.error_server,
                    )
                }
            }
        }
    }

    fun deleteFriend() = viewModelScope.launch {
        val currentState = _state.value
        _state.value = FriendDetailUiState.Loading
        userRepository.deleteFriend(params.userId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.friend_removed
            },
            onFailure = {
                _error.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    R.string.error_search,
                )
                _state.value = currentState
            },
        )
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