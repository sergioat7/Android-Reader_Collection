/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.model.Users
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.friend_action_failure
import reader_collection.app.generated.resources.friend_action_successfully_done

class FriendsViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Loading)
    private val _error = MutableStateFlow<ErrorModel?>(null)
    private val _infoDialogMessageId = MutableStateFlow<StringResource?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<FriendsUiState> = _state
    val error: StateFlow<ErrorModel?> = _error
    val infoDialogMessageId: StateFlow<StringResource?> = _infoDialogMessageId
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
                _infoDialogMessageId.value = Res.string.friend_action_successfully_done
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
                _error.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    Res.string.friend_action_failure,
                )
            },
        )
    }

    fun rejectFriendRequest(friendId: String) = viewModelScope.launch {
        userRepository.rejectFriendRequest(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = Res.string.friend_action_successfully_done
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
                _error.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    Res.string.friend_action_failure,
                )
            },
        )
    }

    fun deleteFriend(friendId: String) = viewModelScope.launch {
        userRepository.deleteFriend(friendId).fold(
            onSuccess = {
                _infoDialogMessageId.value = Res.string.friend_action_successfully_done
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
                _error.value = ErrorModel(
                    Constants.EMPTY_VALUE,
                    Res.string.friend_action_failure,
                )
            },
        )
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = null
        _error.value = null
    }
    //endregion
}