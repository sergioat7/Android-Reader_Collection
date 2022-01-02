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
    private val userApiClient: UserApiClient
): BaseRepository() {

    //region Public properties
    val username: String
        get() = SharedPreferencesHandler.getUserData().username

    val userData: UserData
        get() = SharedPreferencesHandler.getUserData()

    val language: String
        get() = SharedPreferencesHandler.getLanguage()

    val sortParam: String?
        get() = SharedPreferencesHandler.getSortParam()

    val isSortDescending: Boolean
        get() = SharedPreferencesHandler.isSortDescending()

    val themeMode: Int
        get() = SharedPreferencesHandler.getThemeMode()
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

        SharedPreferencesHandler.storeUserData(userData)
        SharedPreferencesHandler.storeCredentials(authData)
    }

    fun storeCredentials(authData: AuthData) {
        SharedPreferencesHandler.storeCredentials(authData)
    }

    fun removeCredentials() {
        SharedPreferencesHandler.removeCredentials()
    }

    fun storePassword(newPassword: String) {
        SharedPreferencesHandler.storePassword(newPassword)
    }

    fun removeUserData() {
        SharedPreferencesHandler.removeUserData()
    }

    fun removePassword() {
        SharedPreferencesHandler.removePassword()
    }

    fun storeLanguage(language: String) {
        SharedPreferencesHandler.setLanguage(language)
    }

    fun storeSortParam(sortParam: String?) {
        SharedPreferencesHandler.setSortParam(sortParam)
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        SharedPreferencesHandler.setIsSortDescending(isSortDescending)
    }

    fun storeThemeMode(themeMode: Int) {
        SharedPreferencesHandler.setThemeMode(themeMode)
    }
    //endregion
}