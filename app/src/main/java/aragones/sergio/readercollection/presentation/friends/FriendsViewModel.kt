/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.RequestStatus
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
class FriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

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

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchFriends() {
        _state.value = FriendsUiState.Loading
        userRepository
            .getFriends()
            .subscribeBy(
                onSuccess = {
                    _state.value = FriendsUiState.Success(it)
                },
            ).addTo(disposables)
    }

    fun acceptFriendRequest(friendId: String) {
        userRepository
            .acceptFriendRequest(friendId)
            .subscribeBy(
                onComplete = {
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
                onError = {
                    _error.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.friend_action_failure,
                    )
                },
            ).addTo(disposables)
    }

    fun rejectFriendRequest(friendId: String) {
        userRepository
            .rejectFriendRequest(friendId)
            .subscribeBy(
                onComplete = {
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
                onError = {
                    _error.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.friend_action_failure,
                    )
                },
            ).addTo(disposables)
    }

    fun deleteFriend(friendId: String) {
        userRepository
            .deleteFriend(friendId)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.friend_action_successfully_done
                },
                onError = {
                    _error.value = ErrorResponse(
                        Constants.EMPTY_VALUE,
                        R.string.friend_action_failure,
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