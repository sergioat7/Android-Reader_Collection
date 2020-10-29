/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.injection.ReaderCollectionApplication
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.repositories.LoginRepository
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
    lateinit var userAPIClient: UserAPIClient
    @Inject
    lateinit var loginRepository: LoginRepository
    @Inject
    lateinit var loginViewModel: LoginViewModel

    //MARK: - Lifecycle methods

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            (application as ReaderCollectionApplication).sharedPreferencesComponent.inject(this)
            return loginViewModel as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}