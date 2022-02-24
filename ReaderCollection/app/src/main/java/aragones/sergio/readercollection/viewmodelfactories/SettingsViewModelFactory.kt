/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.viewmodels.SettingsViewModel
import javax.inject.Inject

class SettingsViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var settingsViewModel: SettingsViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return settingsViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}