/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import aragones.sergio.readercollection.domain.model.Users

sealed class FriendsUiState {
    data object Loading : FriendsUiState()
    data class Success(val friends: Users) : FriendsUiState()
}