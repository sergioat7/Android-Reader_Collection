/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.apiclient.UserAPIClient
import aragones.sergio.readercollection.repositories.base.BaseRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val userAPIClient: UserAPIClient
): BaseRepository() {

    //MARK: - Public properties

    val username: String
        get() = sharedPreferencesHandler.getUserData().username

    val userData: UserData
        get() = sharedPreferencesHandler.getUserData()

    val language: String
        get() = sharedPreferencesHandler.getLanguage()

    val sortParam: String?
        get() = sharedPreferencesHandler.getSortParam()

    val themeMode: Int
        get() = sharedPreferencesHandler.getThemeMode()

    //MARK: - Public methods

    fun registerObserver(username: String, password: String): Completable {
        return userAPIClient.registerObserver(username, password)
    }

    fun deleteUserObserver(): Completable {
        return userAPIClient.deleteUserObserver()
    }

    fun loginObserver(username: String, password: String): Single<LoginResponse> {
        return userAPIClient.loginObserver(username, password)
    }

    fun logoutObserver(): Completable {
        return userAPIClient.logoutObserver()
    }

    fun updatePasswordObserver(newPassword: String): Completable {
        return userAPIClient.updatePasswordObserver(newPassword)
    }

    fun storeLanguage(language: String) {
        sharedPreferencesHandler.setLanguage(language)
    }

    fun storeCredentials(authData: AuthData) {
        sharedPreferencesHandler.storeCredentials(authData)
    }

    fun removeCredentials() {
        sharedPreferencesHandler.removeCredentials()
    }

    fun storePassword(newPassword: String) {
        sharedPreferencesHandler.storePassword(newPassword)
    }

    fun removeUserData() {
        sharedPreferencesHandler.removeUserData()
    }

    fun removePassword() {
        sharedPreferencesHandler.removePassword()
    }

    fun storeSortParam(sortParam: String?) {
        sharedPreferencesHandler.setSortParam(sortParam)
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {

        sharedPreferencesHandler.storeUserData(userData)
        sharedPreferencesHandler.storeCredentials(authData)
    }

    fun storeThemeMode(themeMode: Int) {
        sharedPreferencesHandler.setThemeMode(themeMode)
    }
}