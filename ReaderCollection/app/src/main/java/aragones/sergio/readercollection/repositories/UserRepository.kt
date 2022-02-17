/*
 * Copyright (c) 2021 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 9/1/2021
 */

package aragones.sergio.readercollection.repositories

import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import aragones.sergio.readercollection.models.requests.LoginCredentials
import aragones.sergio.readercollection.models.requests.NewPassword
import aragones.sergio.readercollection.models.responses.LoginResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.network.UserApiService
import aragones.sergio.readercollection.base.BaseRepository
import aragones.sergio.readercollection.utils.SharedPreferencesHandler
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val api: UserApiService
) : BaseRepository() {

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

    val hasBooksTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBooksTutorialBeenShown()

    val hasSearchTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSearchTutorialBeenShown()

    val hasSettingsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSettingsTutorialBeenShown()
    //endregion

    //region Public methods
    fun registerObserver(username: String, password: String): Completable {

        return api
            .register(LoginCredentials(username, password))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun deleteUserObserver(): Completable {

        return api
            .deleteUser()
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun loginObserver(username: String, password: String): Single<LoginResponse> {

        return api
            .login(LoginCredentials(username, password))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun logoutObserver(): Completable {

        return api
            .logout()
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
    }

    fun updatePasswordObserver(newPassword: String): Completable {

        return api
            .updatePassword(NewPassword(newPassword))
            .subscribeOn(ApiManager.SUBSCRIBER_SCHEDULER)
            .observeOn(ApiManager.OBSERVER_SCHEDULER)
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

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown)
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown)
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown)
    }
    //endregion
}