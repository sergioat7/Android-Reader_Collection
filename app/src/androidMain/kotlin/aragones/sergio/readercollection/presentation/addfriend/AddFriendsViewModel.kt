/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.domain.model.User
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddFriendsViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<AddFriendsUiState> = MutableStateFlow(
        AddFriendsUiState.Success(
            users = UsersUi(),
            query = "",
        ),
    )
    private val _error = MutableStateFlow<ErrorModel?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<AddFriendsUiState> = _state
    val error: StateFlow<ErrorModel?> = _error
    //endregion

    //region Public methods
    fun searchUserWith(username: String) = viewModelScope.launch {
        if (username.isNotEmpty()) {
            _state.value = AddFriendsUiState.Loading(username)
            userRepository.getUserWith(username).fold(
                onSuccess = { user ->
                    _state.value = AddFriendsUiState.Success(
                        users = UsersUi(listOf(user.toUi())),
                        query = username,
                    )
                },
                onFailure = {
                    when (it) {
                        is NoSuchElementException -> {
                            _state.value = AddFriendsUiState.Success(
                                users = UsersUi(),
                                query = username,
                            )
                        }
                        else -> {
                            _error.value = ErrorModel(
                                Constants.EMPTY_VALUE,
                                R.string.error_server,
                            )
                            _state.value = AddFriendsUiState.Success(
                                users = UsersUi(),
                                query = username,
                            )
                        }
                    }
                },
            )
        } else {
            _state.value = AddFriendsUiState.Success(
                users = UsersUi(),
                query = username,
            )
        }
    }

    fun requestFriendship(friend: UserUi) = viewModelScope.launch {
        when (val currentState = _state.value) {
            is AddFriendsUiState.Loading -> {}
            is AddFriendsUiState.Success -> {
                _state.value = currentState.copy(
                    users = UsersUi(
                        currentState.users.users.map {
                            if (it.id == friend.id) {
                                friend.copy(isLoading = true)
                            } else {
                                it
                            }
                        },
                    ),
                )
            }
        }
        userRepository.requestFriendship(friend.toDomain()).fold(
            onSuccess = {
                when (val currentState = _state.value) {
                    is AddFriendsUiState.Loading -> {}
                    is AddFriendsUiState.Success -> {
                        _state.value = currentState.copy(
                            users = UsersUi(
                                currentState.users.users.map {
                                    if (it.id == friend.id) {
                                        friend.copy(
                                            status = RequestStatus.APPROVED,
                                            isLoading = false,
                                        )
                                    } else {
                                        it
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
                    R.string.error_search,
                )
                _state.value = AddFriendsUiState.Success(
                    users = UsersUi(),
                    query = "",
                )
            },
        )
    }

    fun closeDialogs() {
        _error.value = null
    }
    //endregion
}

private fun User.toUi(): UserUi = UserUi(
    id = id,
    username = username,
    status = status,
    isLoading = false,
)

private fun UserUi.toDomain(): User = User(
    id = id,
    username = username,
    status = status,
)