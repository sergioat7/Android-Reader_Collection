/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.local

import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData

class UserLocalDataSource(
    private val appInfoProvider: AppInfoProvider,
    private val preferences: SharedPreferencesHandler,
) {

    //region Public properties
    val username: String
        get() = preferences.userData.username

    val userData: UserData
        get() = preferences.userData

    val userId: String
        get() = preferences.credentials.uuid

    val isProfilePublic: Boolean
        get() = preferences.isProfilePublic

    val isAutomaticSyncEnabled: Boolean
        get() = preferences.isAutomaticSyncEnabled

    var language: String
        get() = preferences.language
        set(value) {
            preferences.language = value
        }

    val isLoggedIn: Boolean
        get() = preferences.isLoggedIn

    val sortParam: String?
        get() = preferences.sortParam

    val isSortDescending: Boolean
        get() = preferences.isSortDescending

    val themeMode: Int
        get() = preferences.themeMode
    //endregion

    //region Public methods
    fun logout() {
        preferences.removeUserPreferences()
        preferences.removePassword()
        removeCredentials()
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {
        preferences.userData = userData
        storeCredentials(authData)
    }

    fun storeCredentials(authData: AuthData) {
        preferences.credentials = authData
    }

    fun removeCredentials() {
        preferences.removeCredentials()
    }

    fun storePassword(newPassword: String) {
        preferences.storePassword(newPassword)
    }

    fun removeUserData() {
        preferences.removeUserData()
    }

    fun storePublicProfile(value: Boolean) {
        preferences.isProfilePublic = value
    }

    fun storeAutomaticSync(value: Boolean) {
        preferences.isAutomaticSyncEnabled = value
    }

    fun storeLanguage(language: String) {
        preferences.language = language
        appInfoProvider.changeLocale(language)
    }

    fun storeSortParam(sortParam: String?) {
        preferences.sortParam = sortParam
    }

    fun storeIsSortDescending(isSortDescending: Boolean) {
        preferences.isSortDescending = isSortDescending
    }

    fun storeThemeMode(themeMode: Int) {
        preferences.themeMode = themeMode
    }

    fun applyTheme() {
        appInfoProvider.applyTheme(themeMode)
    }

    fun getCalculatedCurrentVersion(): Int {
        return try {
            val currentVersion = appInfoProvider.getVersion()?.split(".") ?: listOf()
            if (currentVersion.size != 3) return Int.MAX_VALUE

            currentVersion[0].toInt() * 100000 +
                currentVersion[1].toInt() * 1000 +
                currentVersion[2].toInt() * 10
        } catch (_: Exception) {
            Int.MAX_VALUE
        }
    }

    fun getCurrentVersion(): String = appInfoProvider.getVersion() ?: ""
    //endregion
}