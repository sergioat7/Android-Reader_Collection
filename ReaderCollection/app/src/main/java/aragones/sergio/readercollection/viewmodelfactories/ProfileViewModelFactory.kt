/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.injection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.BookAPIClient
import aragones.sergio.readercollection.network.apiclient.FormatAPIClient
import aragones.sergio.readercollection.network.apiclient.StateAPIClient
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.FormatRepository
import aragones.sergio.readercollection.repositories.StateRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.ProfileViewModel
import javax.inject.Inject

class ProfileViewModelFactory(
    private val application: Application
): ViewModelProvider.Factory {

    //MARK: - Public properties

    @Inject
    lateinit var sharedPreferencesHandler: SharedPreferencesHandler
    @Inject
    lateinit var bookAPIClient: BookAPIClient
    @Inject
    lateinit var formatAPIClient: FormatAPIClient
    @Inject
    lateinit var stateAPIClient: StateAPIClient
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
    lateinit var profileViewModel: ProfileViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {

            (application as ReaderCollectionApplication).loginComponent.inject(this)
            return profileViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}