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
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class UserLocalDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferencesHandler,
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
        get() = preferences.userData.username

    val userData: UserData
        get() = preferences.userData

    var language: String
        get() = preferences.language
        set(value) {
            preferences.language = value
        }

    val isLoggedIn: Boolean
        get() = preferences.isLoggedIn && auth.currentUser != null

    val sortParam: String?
        get() = preferences.sortParam

    val isSortDescending: Boolean
        get() = preferences.isSortDescending

    val themeMode: Int
        get() = preferences.themeMode

    val hasBooksTutorialBeenShown: Boolean
        get() = preferences.hasBooksTutorialBeenShown

    val hasDragTutorialBeenShown: Boolean
        get() = preferences.hasDragTutorialBeenShown

    val hasSearchTutorialBeenShown: Boolean
        get() = preferences.hasSearchTutorialBeenShown

    val hasStatisticsTutorialBeenShown: Boolean
        get() = preferences.hasStatisticsTutorialBeenShown

    val hasSettingsTutorialBeenShown: Boolean
        get() = preferences.hasSettingsTutorialBeenShown

    val hasNewBookTutorialBeenShown: Boolean
        get() = preferences.hasNewBookTutorialBeenShown

    val hasBookDetailsTutorialBeenShown: Boolean
        get() = preferences.hasBookDetailsTutorialBeenShown

    var newChangesPopupShown: Boolean
        get() = preferences.newChangesPopupShown
        set(value) {
            preferences.newChangesPopupShown = value
        }
    //endregion

    //region Public methods
    fun logout() {
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

    fun storeLanguage(language: String) {
        preferences.language = language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localeManager?.applicationLocales = LocaleList(Locale.forLanguageTag(language))
        }
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

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {
        preferences.hasBooksTutorialBeenShown = hasBooksTutorialBeenShown
    }

    fun setHasDragTutorialBeenShown(hasDragTutorialBeenShown: Boolean) {
        preferences.hasDragTutorialBeenShown = hasDragTutorialBeenShown
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {
        preferences.hasSearchTutorialBeenShown = hasSearchTutorialBeenShown
    }

    fun setHasStatisticsTutorialBeenShown(hasStatisticsTutorialBeenShown: Boolean) {
        preferences.hasStatisticsTutorialBeenShown = hasStatisticsTutorialBeenShown
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {
        preferences.hasSettingsTutorialBeenShown = hasSettingsTutorialBeenShown
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {
        preferences.hasNewBookTutorialBeenShown = hasNewBookTutorialBeenShown
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {
        preferences.hasBookDetailsTutorialBeenShown = hasBookDetailsTutorialBeenShown
    }

    fun getCurrentVersion(): Int {
        return try {
            val currentVersion = context
                .packageManager
                .getPackageInfo(context.packageName, 0)
                .versionName
                ?.split(".") ?: listOf()
            if (currentVersion.size != 3) return 0

            currentVersion[0].toInt() * 100000 +
                currentVersion[1].toInt() * 1000 +
                currentVersion[2].toInt() * 10
        } catch (e: Exception) {
            0
        }
    }
    //endregion
}