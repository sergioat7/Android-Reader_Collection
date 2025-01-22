/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.presentation.ui.modals.syncapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class PopupSyncAppViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
) : BaseViewModel() {

    //region Private properties
    private val _loginError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val loginError: LiveData<ErrorResponse?> = _loginError
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun loadContent() {
        booksRepository
            .loadBooks()
            .subscribeBy(
                onComplete = {
                    _loginError.value = null
                },
                onError = {
                    _loginError.value = ErrorResponse(Constants.EMPTY_VALUE, R.string.error_server)
                },
            ).addTo(disposables)
    }
    //endregion
}