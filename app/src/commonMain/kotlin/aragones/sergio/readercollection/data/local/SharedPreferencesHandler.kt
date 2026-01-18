/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.data.local

import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import com.aragones.sergio.util.Preferences
import kotlinx.serialization.json.Json

class SharedPreferencesHandler(
    private val appInfoProvider: AppInfoProvider,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
) {

    //region Public properties
    var language: String
        get() {
            return sharedPreferencesProvider.readString(
                Preferences.LANGUAGE_PREFERENCE_NAME,
            ) ?: run {
                appInfoProvider.getCurrentLanguage().also {
                    language = it
                }
            }
        }
        set(value) =
            sharedPreferencesProvider.writeString(Preferences.LANGUAGE_PREFERENCE_NAME, value)
    var credentials: AuthData
        get() {
            return sharedPreferencesProvider
                .readString(Preferences.AUTH_DATA_PREFERENCES_NAME, true)
                ?.let { Json.decodeFromString<AuthData>(it) }
                ?: run { AuthData("") }
        }
        set(value) = sharedPreferencesProvider.writeString(
            Preferences.AUTH_DATA_PREFERENCES_NAME,
            Json.encodeToString(value),
            true,
        )
    var userData: UserData
        get() {
            return sharedPreferencesProvider
                .readString(Preferences.USER_DATA_PREFERENCES_NAME, true)
                ?.let { Json.decodeFromString<UserData>(it) }
                ?: run { UserData("", "") }
        }
        set(value) = sharedPreferencesProvider.writeString(
            Preferences.USER_DATA_PREFERENCES_NAME,
            Json.encodeToString(value),
            true,
        )
    val isLoggedIn: Boolean
        get() = credentials.uuid.isNotEmpty()
    var isProfilePublic: Boolean
        get() = sharedPreferencesProvider.readBoolean(
            Preferences.PUBLIC_PROFILE_PREFERENCE_NAME,
            false,
        )
        set(value) = sharedPreferencesProvider.writeBoolean(
            Preferences.PUBLIC_PROFILE_PREFERENCE_NAME,
            value,
        )
    var isAutomaticSyncEnabled: Boolean
        get() = sharedPreferencesProvider.readBoolean(
            Preferences.AUTOMATIC_SYNC_PREFERENCE_NAME,
            true,
        )
        set(value) = sharedPreferencesProvider.writeBoolean(
            Preferences.AUTOMATIC_SYNC_PREFERENCE_NAME,
            value,
        )
    var sortParam: String?
        get() = sharedPreferencesProvider.readString(Preferences.SORT_PARAM_PREFERENCE_NAME)
        set(value) =
            sharedPreferencesProvider.writeString(Preferences.SORT_PARAM_PREFERENCE_NAME, value)
    var themeMode: Int
        get() = sharedPreferencesProvider.readInt(Preferences.THEME_MODE_PREFERENCE_NAME, 0)
        set(value) =
            sharedPreferencesProvider.writeInt(Preferences.THEME_MODE_PREFERENCE_NAME, value)
    var isSortDescending: Boolean
        get() = sharedPreferencesProvider.readBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, false)
        set(value) =
            sharedPreferencesProvider.writeBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, value)
    //endregion

    //region Public methods
    fun removeCredentials() {
        sharedPreferencesProvider.removeValues(
            keys = listOf(Preferences.AUTH_DATA_PREFERENCES_NAME),
            isEncrypted = true,
        )
    }

    fun storePassword(password: String) {
        userData = UserData(userData.username, password)
    }

    fun removePassword() {
        userData = UserData(userData.username, "")
    }

    fun removeUserData() {
        sharedPreferencesProvider.removeValues(
            keys = listOf(Preferences.USER_DATA_PREFERENCES_NAME),
            isEncrypted = true,
        )
    }

    fun removeUserPreferences() {
        sharedPreferencesProvider.removeValues(
            keys = listOf(
                Preferences.LANGUAGE_PREFERENCE_NAME,
                Preferences.PUBLIC_PROFILE_PREFERENCE_NAME,
                Preferences.AUTOMATIC_SYNC_PREFERENCE_NAME,
                Preferences.SORT_PARAM_PREFERENCE_NAME,
                Preferences.THEME_MODE_PREFERENCE_NAME,
                Preferences.SORT_ORDER_PREFERENCE_NAME,
            ),
            isEncrypted = true,
        )
    }
    //endregion
}