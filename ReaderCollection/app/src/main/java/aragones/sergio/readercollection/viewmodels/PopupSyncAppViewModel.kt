/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
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

        booksRepository.loadBooks(success = {
            _loginError.value = null
        }, failure = {
            _loginError.value = it
        })
    }
    //endregion
}