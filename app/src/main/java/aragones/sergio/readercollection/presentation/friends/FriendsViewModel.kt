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
import aragones.sergio.readercollection.domain.model.Users
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.fold
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
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
        _state.value = FriendsUiState.Success(Users(friends))
    }

    fun acceptFriendRequest(friendId: String) = viewModelScope.launch {
        userRepository.acceptFriendRequest(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.friend_action_successfully_done
                _state.update {
                    when (it) {
                        FriendsUiState.Loading -> it
                        is FriendsUiState.Success -> it.copy(
                            friends = Users(
                                it.friends.users.map { friend ->
                                    if (friend.id == friendId) {
                                        friend.copy(status = RequestStatus.APPROVED)
                                    } else {
                                        friend
                                    }
                                },
                            ),
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
                _state.update {
                    when (it) {
                        FriendsUiState.Loading -> it
                        is FriendsUiState.Success -> it.copy(
                            friends = Users(
                                it.friends.users.filter { friend -> friend.id != friendId },
                            ),
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
                _state.update {
                    when (it) {
                        FriendsUiState.Loading -> it
                        is FriendsUiState.Success -> it.copy(
                            friends = Users(
                                it.friends.users.filter { friend -> friend.id != friendId },
                            ),
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