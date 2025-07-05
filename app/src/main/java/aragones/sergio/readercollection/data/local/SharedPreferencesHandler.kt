/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.data.local.model.AuthData
import aragones.sergio.readercollection.data.local.model.UserData
import com.aragones.sergio.util.Preferences
import com.squareup.moshi.Moshi
import java.util.Locale

class SharedPreferencesHandler {

    //region Private properties
    private val appPreferences = ReaderCollectionApplication.context.getSharedPreferences(
        Preferences.PREFERENCES_NAME,
        Context.MODE_PRIVATE,
    )
    private val editor = appPreferences.edit()
    private val appEncryptedPreferences = EncryptedSharedPreferences.create(
        Preferences.ENCRYPTED_PREFERENCES_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        ReaderCollectionApplication.context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
    private val encryptedEditor = appEncryptedPreferences.edit()
    private val moshi = Moshi.Builder().build()
    //endregion

    //region Public properties
    var language: String
        get() {
            return appPreferences.getString(Preferences.LANGUAGE_PREFERENCE_NAME, null) ?: run {
                language = Locale.getDefault().language
                language
            }
        }
        set(value) = editor.setString(Preferences.LANGUAGE_PREFERENCE_NAME, value)
    var credentials: AuthData
        get() {
            return appEncryptedPreferences
                .getString(Preferences.AUTH_DATA_PREFERENCES_NAME, null)
                ?.let {
                    moshi.adapter(AuthData::class.java).fromJson(it)
                } ?: run {
                AuthData("")
            }
        }
        set(value) = encryptedEditor.setString(
            Preferences.AUTH_DATA_PREFERENCES_NAME,
            moshi.adapter(AuthData::class.java).toJson(value),
        )
    var userData: UserData
        get() {
            return appEncryptedPreferences
                .getString(Preferences.USER_DATA_PREFERENCES_NAME, null)
                ?.let {
                    moshi.adapter(UserData::class.java).fromJson(it)
                } ?: run {
                UserData("", "")
            }
        }
        set(value) = encryptedEditor.setString(
            Preferences.USER_DATA_PREFERENCES_NAME,
            moshi.adapter(UserData::class.java).toJson(value),
        )
    val isLoggedIn: Boolean
        get() = credentials.uuid.isNotEmpty()
    var isProfilePublic: Boolean
        get() = appPreferences.getBoolean(Preferences.PUBLIC_PROFILE_PREFERENCE_NAME, false)
        set(value) = editor.setBoolean(Preferences.PUBLIC_PROFILE_PREFERENCE_NAME, value)
    var isAutomaticSyncEnabled: Boolean
        get() = appPreferences.getBoolean(Preferences.AUTOMATIC_SYNC_PREFERENCE_NAME, true)
        set(value) = editor.setBoolean(Preferences.AUTOMATIC_SYNC_PREFERENCE_NAME, value)
    var sortParam: String?
        get() = appPreferences.getString(Preferences.SORT_PARAM_PREFERENCE_NAME, null)
        set(value) = editor.setString(Preferences.SORT_PARAM_PREFERENCE_NAME, value)
    var themeMode: Int
        get() = appPreferences.getInt(Preferences.THEME_MODE_PREFERENCE_NAME, 0)
        set(value) = editor.setInt(Preferences.THEME_MODE_PREFERENCE_NAME, value)
    var isSortDescending: Boolean
        get() = appPreferences.getBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, false)
        set(value) = editor.setBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, value)
    //endregion

    //region Public methods
    fun removeCredentials() {
        encryptedEditor.remove(Preferences.AUTH_DATA_PREFERENCES_NAME)?.apply()
    }

    fun storePassword(password: String) {
        userData = UserData(userData.username, password)
    }

    fun removePassword() {
        userData = UserData(userData.username, "")
    }

    fun removeUserData() {
        encryptedEditor.remove(Preferences.USER_DATA_PREFERENCES_NAME)?.apply()
    }
    //endregion
}