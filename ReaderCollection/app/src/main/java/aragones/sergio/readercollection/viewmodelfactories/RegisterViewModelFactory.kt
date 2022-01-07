/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/10/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.viewmodels.RegisterViewModel
import javax.inject.Inject

class RegisterViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //region Public properties
    @Inject
    lateinit var formatRepository: FormatRepository
    @Inject
    lateinit var stateRepository: StateRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var registerViewModel: RegisterViewModel
    //endregion

    //region Lifecycle methods
    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return registerViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    //endregion
}