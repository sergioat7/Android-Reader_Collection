/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import aragones.sergio.readercollection.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class AddFriendsViewModel @Inject constructor() : BaseViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<AddFriendsUiState> = MutableStateFlow(
        AddFriendsUiState.Success(
            users = emptyList(),
            query = "",
        ),
    )
    //endregion

    //region Public properties
    val state: StateFlow<AddFriendsUiState> = _state
    //endregion
}