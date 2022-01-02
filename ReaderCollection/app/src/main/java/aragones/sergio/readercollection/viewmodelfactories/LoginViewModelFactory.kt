/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.LoginViewModel
import javax.inject.Inject

class LoginViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //MARK: - Public properties

    @Inject
    lateinit var sharedPreferencesHandler: SharedPreferencesHandler
    @Inject
    lateinit var bookAPIClient: BookAPIClient
    @Inject
    lateinit var userAPIClient: UserAPIClient
    @Inject
    lateinit var booksRepository: BooksRepository
    @Inject
    lateinit var formatRepository: FormatRepository
    @Inject
    lateinit var stateRepository: StateRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var loginViewModel: LoginViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            (application as ReaderCollectionApplication).appComponent.inject(this)
            return loginViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}