/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.viewmodelfactories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.repositories.LoginRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import aragones.sergio.readercollection.viewmodels.LoginViewModel

class LoginViewModelFactory(private val context: Context?) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {

            val sharedPreferencesHandler = SharedPreferencesHandler(context)
            return LoginViewModel(
                LoginRepository(
                    sharedPreferencesHandler,
                    UserAPIClient(sharedPreferencesHandler)
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}