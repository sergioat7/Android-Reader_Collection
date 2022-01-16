/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2021
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.viewmodels.PopupSyncAppViewModel
import javax.inject.Inject

class PopupSyncAppViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var popupSyncAppViewModel: PopupSyncAppViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PopupSyncAppViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return popupSyncAppViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}