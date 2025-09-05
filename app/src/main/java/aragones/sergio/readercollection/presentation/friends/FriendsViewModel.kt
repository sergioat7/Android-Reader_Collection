/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.UserRepository
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.fold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Loading)
    private val _error = MutableStateFlow<ErrorResponse?>(null)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: StateFlow<FriendsUiState> = _state
    val error: StateFlow<ErrorResponse?> = _error
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Public methods
    fun fetchFriends() = viewModelScope.launch {
        _state.value = FriendsUiState.Loading
        val friends = userRepository.getFriends()
        _state.value = FriendsUiState.Success(friends)
    }

    fun acceptFriendRequest(friendId: String) = viewModelScope.launch {
        userRepository.acceptFriendRequest(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.friend_action_successfully_done
                when (val currentState = _state.value) {
                    is FriendsUiState.Loading -> {}
                    is FriendsUiState.Success -> {
                        _state.value = currentState.copy(
                            friends = currentState.friends.map {
                                if (it.id == friendId) {
                                    it.copy(status = RequestStatus.APPROVED)
                                } else {
                                    it
                                }
                            },
                        )
                    }
                }
            },
            onFailure = {
                _error.value = ErrorResponse(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                )
            },
        )
    }

    fun rejectFriendRequest(friendId: String) = viewModelScope.launch {
        userRepository.rejectFriendRequest(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.friend_action_successfully_done
                when (val currentState = _state.value) {
                    is FriendsUiState.Loading -> {}
                    is FriendsUiState.Success -> {
                        _state.value = currentState.copy(
                            friends = currentState.friends.filter { it.id != friendId },
                        )
                    }
                }
            },
            onFailure = {
                _error.value = ErrorResponse(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                )
            },
        )
    }

    fun deleteFriend(friendId: String) = viewModelScope.launch {
        userRepository.deleteFriend(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.friend_action_successfully_done
                when (val currentState = _state.value) {
                    is FriendsUiState.Loading -> {}
                    is FriendsUiState.Success -> {
                        _state.value = currentState.copy(
                            friends = currentState.friends.filter { it.id != friendId },
                        )
                    }
                }
            },
            onFailure = {
                _error.value = ErrorResponse(
                    Constants.EMPTY_VALUE,
                    R.string.friend_action_failure,
                )
            },
        )
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
        _error.value = null
    }
    //endregion
}