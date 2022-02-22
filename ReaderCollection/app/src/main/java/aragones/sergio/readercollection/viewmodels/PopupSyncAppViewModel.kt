/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class PopupSyncAppViewModel @Inject constructor(
    private val booksRepository: BooksRepository
): BaseViewModel() {

    //region Private properties
    private val _loginError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val loginError: LiveData<ErrorResponse?> = _loginError
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun loadContent() {

        booksRepository.loadBooksObserver().subscribeBy(
            onComplete = {
                _loginError.value = null
            },
            onError = {
                _loginError.value = ErrorResponse("", R.string.error_database)
            }
        ).addTo(disposables)
    }
    //endregion
}