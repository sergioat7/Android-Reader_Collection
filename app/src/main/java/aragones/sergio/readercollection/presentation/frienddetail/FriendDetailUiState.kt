/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import aragones.sergio.readercollection.domain.model.User

sealed class FriendDetailUiState {
    data object Loading : FriendDetailUiState()
    data class Success(val friend: User) : FriendDetailUiState()
}