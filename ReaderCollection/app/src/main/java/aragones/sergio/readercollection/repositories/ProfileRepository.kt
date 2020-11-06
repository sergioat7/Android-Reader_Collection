/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val userAPIClient: UserAPIClient
) {

    //MARK: - Public methods

    fun logout(): Completable {
        return userAPIClient.logoutObserver()
    }

    fun updatePassword(newPassword: String): Completable {
        return userAPIClient.updatePasswordObserver(newPassword)
    }

    fun login(username: String, password: String): Single<LoginResponse> {
        return userAPIClient.loginObserver(username, password)
    }

    fun deleteUser(): Completable {
        return userAPIClient.deleteUserObserver()
    }

    fun storePassword(newPassword: String) {
        sharedPreferencesHandler.storePassword(newPassword)
    }

    fun storeCredentials(authData: AuthData) {
        sharedPreferencesHandler.storeCredentials(authData)
    }

    fun removePassword() {
        sharedPreferencesHandler.removePassword()
    }

    fun removeUserData() {
        sharedPreferencesHandler.removeUserData()
    }

    fun removeCredentials() {
        sharedPreferencesHandler.removeCredentials()
    }
}