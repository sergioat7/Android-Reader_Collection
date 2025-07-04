/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _isLoading: MutableState<Boolean> = mutableStateOf(false)
    private val _logOut = MutableStateFlow(false)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val isLoading: State<Boolean> = _isLoading
    val logOut: StateFlow<Boolean> = _logOut
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun logout() {
        _isLoading.value = true
        userRepository.logout()
        booksRepository
            .resetTable()
            .subscribeBy(
                onComplete = {
                    _isLoading.value = false
                    _logOut.value = true
                },
                onError = {
                    _isLoading.value = false
                    _logOut.value = true
                },
            ).addTo(disposables)
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _confirmationDialogMessageId.value = -1
    }
    //endregion
}