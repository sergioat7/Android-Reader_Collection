/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.data.local

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import aragones.sergio.readercollection.data.remote.ApiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    //region Private properties
    private var localeManager: LocaleManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java)
        } else {
            null
        }
    //endregion

    //region Public properties
    val username: String
        get() = SharedPreferencesHandler.userData.username

    val userData: UserData
        get() = SharedPreferencesHandler.userData

    var language: String
        get() = SharedPreferencesHandler.language
        set(value) {
            SharedPreferencesHandler.language = value
        }

    val isLoggedIn: Boolean
        get() = SharedPreferencesHandler.isLoggedIn

    val sortParam: String?
        get() = SharedPreferencesHandler.sortParam

    val isSortDescending: Boolean
        get() = SharedPreferencesHandler.isSortDescending

    val themeMode: Int
        get() = SharedPreferencesHandler.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasBooksTutorialBeenShown

    val hasDragTutorialBeenShown: Boolean
        get() = SharedPreferencesHandler.hasDragTutorialBeenShown

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

    var newChangesPopupShown: Boolean
        get() = SharedPreferencesHandler.newChangesPopupShown
        set(value) {
            SharedPreferencesHandler.newChangesPopupShown = value
        }
    //endregion

    //region Public methods
    fun logout() {
        SharedPreferencesHandler.removePassword()
        removeCredentials()
        SharedPreferencesHandler.logout()
    }

    fun storeLoginData(userData: UserData, authData: AuthData) {
        SharedPreferencesHandler.userData = userData
        storeCredentials(authData)
    }

    fun storeCredentials(authData: AuthData) {
        SharedPreferencesHandler.credentials = authData
        ApiManager.accessToken = authData.token
    }

    fun removeCredentials() {
        SharedPreferencesHandler.removeCredentials()
        ApiManager.accessToken = ""
    }

    fun storePassword(newPassword: String) {
        SharedPreferencesHandler.storePassword(newPassword)
    }

    fun removeUserData() {
        SharedPreferencesHandler.removeUserData()
    }

    fun storeLanguage(language: String) {
        SharedPreferencesHandler.language = language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(language))
        }
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

    fun setHasDragTutorialBeenShown(hasDragTutorialBeenShown: Boolean) {
        SharedPreferencesHandler.hasDragTutorialBeenShown = hasDragTutorialBeenShown
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