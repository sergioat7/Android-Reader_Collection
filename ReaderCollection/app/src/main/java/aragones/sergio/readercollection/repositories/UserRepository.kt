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
        get() = SharedPreferencesHandler.userData.username

    val userData: UserData
        get() = SharedPreferencesHandler.userData

    val language: String
        get() = SharedPreferencesHandler.language

    val sortParam: String?
        get() = SharedPreferencesHandler.sortParam

    val isSortDescending: Boolean
        get() = SharedPreferencesHandler.isSortDescending

    val themeMode: Int
        get() = SharedPreferencesHandler.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBooksTutorialBeenShown

    val hasSearchTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSearchTutorialBeenShown

    val hasStatisticsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasStatisticsTutorialBeenShown

    val hasSettingsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasSettingsTutorialBeenShown

    val hasNewBookTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasNewBookTutorialBeenShown

    val hasBookDetailsTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBookDetailsTutorialBeenShown
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

        SharedPreferencesHandler.userData = userData
        SharedPreferencesHandler.credentials = authData
    }

    fun storeCredentials(authData: AuthData) {
        SharedPreferencesHandler.credentials = authData
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
        SharedPreferencesHandler.language = language
    }

    fun storeSortParam(sortParam: String?) {
        SharedPreferencesHandler.sortParam = sortParam
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        SharedPreferencesHandler.isSortDescending = isSortDescending
    }

    fun storeThemeMode(themeMode: Int) {
        SharedPreferencesHandler.themeMode = themeMode
    }

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasBooksTutorialBeenShown = hasBooksTutorialBeenShown
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasSearchTutorialBeenShown = hasSearchTutorialBeenShown
    }

    fun setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasStatisticsTutorialBeenShown = hasStatisticsTutorialBeenShown
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasSettingsTutorialBeenShown = hasSettingsTutorialBeenShown
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasNewBookTutorialBeenShown = hasNewBookTutorialBeenShown
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasBookDetailsTutorialBeenShown = hasBookDetailsTutorialBeenShown
    }
    //endregion
}