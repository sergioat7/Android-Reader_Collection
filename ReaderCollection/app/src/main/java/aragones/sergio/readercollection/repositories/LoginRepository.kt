/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Single

class LoginRepository(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val userAPIClient: UserAPIClient
) {

    //MARK: - Public properties

    val username: String?
        get() = sharedPreferencesHandler.getUserData().username

    //MARK: - Public methods

    fun login(username: String, password: String): Single<LoginResponse> {
        return userAPIClient.loginObserver(username, password)
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {

        sharedPreferencesHandler.storeUserData(userData)
        sharedPreferencesHandler.storeCredentials(authData)
    }
}