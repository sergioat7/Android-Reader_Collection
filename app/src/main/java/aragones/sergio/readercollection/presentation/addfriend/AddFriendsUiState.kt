/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import aragones.sergio.readercollection.data.remote.model.RequestStatus

sealed class AddFriendsUiState {

    abstract val query: String

    data class Loading(override val query: String) : AddFriendsUiState()
    data class Success(val users: List<UserUi>, override val query: String) : AddFriendsUiState()
}

data class UserUi(
    val id: String,
    val username: String,
    val status: RequestStatus,
    val isLoading: Boolean,
)
