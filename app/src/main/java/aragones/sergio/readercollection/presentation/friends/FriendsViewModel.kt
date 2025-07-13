/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
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
    //endregion

    //region Public properties
    val state: StateFlow<FriendsUiState> = _state
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
    //endregion
}