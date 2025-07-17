/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    state: SavedStateHandle,
) : BaseViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.FriendDetail>()
    private var _state: MutableStateFlow<FriendDetailUiState> =
        MutableStateFlow(FriendDetailUiState.Loading)
    //endregion

    //region Public properties
    val state: StateFlow<FriendDetailUiState> = _state
    //endregion

    //region Public methods
    fun fetchFriend() {
    }
    //endregion
}