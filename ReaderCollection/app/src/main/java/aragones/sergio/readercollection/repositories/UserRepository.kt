/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.apiclient.UserApiClient
import aragones.sergio.readercollection.repositories.base.BaseRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val sharedPreferencesHandler: SharedPreferencesHandler,
    private val userApiClient: UserApiClient
): BaseRepository() {

    //region Public properties
    val username: String
        get() = sharedPreferencesHandler.getUserData().username

    val userData: UserData
        get() = sharedPreferencesHandler.getUserData()

    val language: String
        get() = sharedPreferencesHandler.getLanguage()

    val sortParam: String?
        get() = sharedPreferencesHandler.getSortParam()

    val isSortDescending: Boolean
        get() = sharedPreferencesHandler.isSortDescending()

    val themeMode: Int
        get() = sharedPreferencesHandler.getThemeMode()
    //endregion

    //region Public methods
    fun registerObserver(username: String, password: String): Completable {
        return userApiClient.registerObserver(username, password)
    }

    fun deleteUserObserver(): Completable {
        return userApiClient.deleteUserObserver()
    }

    fun loginObserver(username: String, password: String): Single<LoginResponse> {
        return userApiClient.loginObserver(username, password)
    }

    fun logoutObserver(): Completable {
        return userApiClient.logoutObserver()
    }

    fun updatePasswordObserver(newPassword: String): Completable {
        return userApiClient.updatePasswordObserver(newPassword)
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {

        sharedPreferencesHandler.storeUserData(userData)
        sharedPreferencesHandler.storeCredentials(authData)
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

    fun storeLanguage(language: String) {
        sharedPreferencesHandler.setLanguage(language)
    }

    fun storeSortParam(sortParam: String?) {
        sharedPreferencesHandler.setSortParam(sortParam)
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        sharedPreferencesHandler.setIsSortDescending(isSortDescending)
    }

    fun storeThemeMode(themeMode: Int) {
        sharedPreferencesHandler.setThemeMode(themeMode)
    }
    //endregion
}