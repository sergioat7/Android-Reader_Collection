/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 6/11/2020
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.local.UserLocalDataSource
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.UserRemoteDataSource
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.base.BaseRepository
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource
) : BaseRepository() {

    //region Private properties
    private val EMPTY_VALUE = ""
    private val GOOGLE_USER_TEST = "googleTest"
    private val GOOGLE_PASSWORD_TEST = "d9MqzK3k1&07"
    //endregion

    //region Public properties
    val username: String
        get() = userLocalDataSource.username

    val userData: UserData
        get() = userLocalDataSource.userData

    var language: String
        get() = userLocalDataSource.language
        set(value) {
            userLocalDataSource.language = value
        }

    val isLoggedIn: Boolean
        get() = userLocalDataSource.isLoggedIn

    val sortParam: String?
        get() = userLocalDataSource.sortParam

    val isSortDescending: Boolean
        get() = userLocalDataSource.isSortDescending

    val themeMode: Int
        get() = userLocalDataSource.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasBooksTutorialBeenShown

    val hasDragTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasDragTutorialBeenShown

    val hasSearchTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasSearchTutorialBeenShown

    val hasStatisticsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasStatisticsTutorialBeenShown

    val hasSettingsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasSettingsTutorialBeenShown

    val hasNewBookTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasNewBookTutorialBeenShown

    val hasBookDetailsTutorialBeenShown: Boolean
        get() = userLocalDataSource.hasBookDetailsTutorialBeenShown

    var newChangesPopupShown: Boolean
        get() = userLocalDataSource.newChangesPopupShown
        set(value) {
            userLocalDataSource.newChangesPopupShown = value
        }
    //endregion

    //region Public methods
    fun login(
        username: String,
        password: String,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {
        userRemoteDataSource.login(username, password, success = { token ->

            if (username == GOOGLE_USER_TEST && password == GOOGLE_PASSWORD_TEST) {
                val userData =
                    UserData(GOOGLE_USER_TEST, GOOGLE_PASSWORD_TEST, true)
                val authData = AuthData("-")
                userLocalDataSource.storeLoginData(userData, authData)
                success()
            } else if (userData.username == username && userData.password == password) {
                val userData = UserData(username, password, true)
                val authData = AuthData(token)
                userLocalDataSource.storeLoginData(userData, authData)
                success()
            } else {
                failure(ErrorResponse(EMPTY_VALUE, R.string.wrong_credentials))
            }

//            val userData = UserData(username, password, true)
//            val authData = AuthData(token)
//            userLocalDataSource.storeLoginData(userData, authData)
//            success()
        }, failure = failure)
    }

    fun logout() {

        userLocalDataSource.logout()
        userRemoteDataSource.logout()
    }

    fun register(
        username: String,
        password: String,
        success: () -> Unit,
        failure: (ErrorResponse) -> Unit
    ) {

        userRemoteDataSource.register(username, password, success = {

            val userData = UserData(username, password, false)
            val authData = AuthData("-")
            userLocalDataSource.storeLoginData(userData, authData)
            success()
        }, failure)
    }

    fun updatePassword(password: String, success: () -> Unit, failure: (ErrorResponse) -> Unit) {

        userRemoteDataSource.updatePassword(password, success = {

            userLocalDataSource.storePassword(password)
            userRemoteDataSource.login(userLocalDataSource.username, password, success = { token ->

                userLocalDataSource.storeCredentials(AuthData(token))
                success()
            }, failure)
        }, failure)
    }

    fun deleteUser(success: () -> Unit, failure: (ErrorResponse) -> Unit) {

        userRemoteDataSource.deleteUser(success = {

            userLocalDataSource.removeUserData()
            userLocalDataSource.removeCredentials()
            success()
        }, failure)
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {
        userLocalDataSource.storeLoginData(userData, authData)
    }

    fun storeCredentials(authData: AuthData) {
        userLocalDataSource.storeCredentials(authData)
    }

    fun storePassword(newPassword: String) {
        userLocalDataSource.storePassword(newPassword)
    }

    fun removeUserData() {
        userLocalDataSource.removeUserData()
    }

    fun storeLanguage(language: String) {
        userLocalDataSource.storeLanguage(language)
    }

    fun storeSortParam(sortParam: String?) {
        userLocalDataSource.storeSortParam(sortParam)
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        userLocalDataSource.storeIsSortDescending(isSortDescending)
    }

    fun storeThemeMode(themeMode: Int) {
        userLocalDataSource.storeThemeMode(themeMode)
    }

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown)
    }

    fun setHasDragTutorialBeenShown(hasDragTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasDragTutorialBeenShown(hasDragTutorialBeenShown)
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown)
    }

    fun setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown)
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown)
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown)
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {
        userLocalDataSource.setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown)
    }
    //endregion
}