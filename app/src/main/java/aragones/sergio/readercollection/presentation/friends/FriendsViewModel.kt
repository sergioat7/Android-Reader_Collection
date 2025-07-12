/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import aragones.sergio.readercollection.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class FriendsViewModel @Inject constructor() : BaseViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<FriendsUiState> = MutableStateFlow(FriendsUiState.Loading)
    //endregion

    //region Public properties
    val state: StateFlow<FriendsUiState> = _state
    //endregion

    //region Public methods
    fun fetchFriends() {
    }
    //endregion
}