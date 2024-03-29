/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.ui.modals.syncapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.ui.base.BaseViewModel
import com.aragones.sergio.data.business.ErrorResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PopupSyncAppViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

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