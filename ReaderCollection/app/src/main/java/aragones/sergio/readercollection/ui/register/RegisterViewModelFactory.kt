/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.ui.register

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.data.source.UserRepository
import javax.inject.Inject

class RegisterViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //region Public properties
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