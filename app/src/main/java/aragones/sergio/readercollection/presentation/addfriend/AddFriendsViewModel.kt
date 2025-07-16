/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AddFriendsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<AddFriendsUiState> = MutableStateFlow(
        AddFriendsUiState.Success(
            users = emptyList(),
            query = "",
        ),
    )
    private val _error = MutableStateFlow<ErrorResponse?>(null)
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        userRepository.onDestroy()
    }
    //endregion

    //region Public properties
    val state: StateFlow<AddFriendsUiState> = _state
    val error: StateFlow<ErrorResponse?> = _error
    //endregion

    //region Public methods
    fun searchUserWith(username: String) {
        if (username.isNotEmpty()) {
            _state.value = AddFriendsUiState.Loading(_state.value.query)
            userRepository
                .getUserWith(username)
                .subscribeBy(
                    onSuccess = { user ->
                        _state.value = AddFriendsUiState.Success(
                            users = listOf(user.toUi()),
                            query = username,
                        )
                    },
                    onError = {
                        when (it) {
                            is NoSuchElementException -> {
                                _state.value = AddFriendsUiState.Success(
                                    users = emptyList(),
                                    query = username,
                                )
                            }
                            else -> {
                                _error.value = ErrorResponse(
                                    Constants.EMPTY_VALUE,
                                    R.string.error_server,
                                )
                                _state.value = AddFriendsUiState.Success(
                                    users = emptyList(),
                                    query = username,
                                )
                            }
                        }
                    },
                ).addTo(disposables)
        } else {
            _state.value = AddFriendsUiState.Success(
                users = emptyList(),
                query = username,
            )
        }
    }

    fun requestFriendship(friend: UserUi) {
        when (val currentState = _state.value) {
            is AddFriendsUiState.Loading -> {}
            is AddFriendsUiState.Success -> {
                _state.value = currentState.copy(
                    users = currentState.users.map {
                        if (it.id == friend.id) {
                            friend.copy(isLoading = true)
                        } else {
                            it
                        }
                    },
                )
            }
        }
        userRepository
            .requestFriendship(friend.toDomain())
            .subscribeBy(
                onComplete = {
                    when (val currentState = _state.value) {
                        is AddFriendsUiState.Loading -> {}
                        is AddFriendsUiState.Success -> {
                            _state.value = currentState.copy(
                                users = currentState.users.map {
                                    if (it.id == friend.id) {
                                        friend.copy(
                                            status = RequestStatus.APPROVED,
                                            isLoading = false,
                                        )
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
                        R.string.error_search,
                    )
                    _state.value = AddFriendsUiState.Success(
                        users = emptyList(),
                        query = "",
                    )
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _error.value = null
    }
    //endregion
}

fun User.toUi(): UserUi = UserUi(
    id = id,
    username = username,
    status = status,
    isLoading = false,
)

fun UserUi.toDomain(): User = User(
    id = id,
    username = username,
    status = status,
)