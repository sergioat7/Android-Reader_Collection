/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.presentation.navigation.Route
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class FriendDetailViewModel @Inject constructor(
    state: SavedStateHandle,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.FriendDetail>()
    private var _state: MutableStateFlow<FriendDetailUiState> =
        MutableStateFlow(FriendDetailUiState.Loading)
    private val _error = MutableStateFlow<ErrorResponse?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<FriendDetailUiState> = _state
    val error: StateFlow<ErrorResponse?> = _error
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchFriend() {
        userRepository
            .getFriend(params.userId)
            .subscribeBy(
                onSuccess = {
                    _state.value = FriendDetailUiState.Success(it)
                },
                onError = {
                    when (it) {
                        is NoSuchElementException -> {
                            _error.value = ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.no_friends_found,
                            )
                        }
                        else -> {
                            _error.value = ErrorResponse(
                                Constants.EMPTY_VALUE,
                                R.string.error_server,
                            )
                        }
                    }
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _error.value = null
    }
    //endregion
}