/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.SharedPreferences
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import com.google.gson.Gson
import java.util.*
import javax.inject.Inject

class SharedPreferencesHandler @Inject constructor(
    private val sharedPreferences: SharedPreferences?
) {

    //MARK: Private properties

    private val gson = Gson()

    //MARK: Public methods

    fun getLanguage(): String {

        sharedPreferences?.getString(Preferences.LANGUAGE_PREFERENCE_NAME, null)?.let {
            return it
        } ?: run {

            val locale = Locale.getDefault().language
            setLanguage(locale)
            return locale
        }
    }

    fun setLanguage(language: String) {

        if (sharedPreferences != null) {
            with (sharedPreferences.edit()) {

                putString(Preferences.LANGUAGE_PREFERENCE_NAME, language)
                commit()
            }
        }
    }

    fun getCredentials(): AuthData {

        val authDataJson = sharedPreferences?.getString(Preferences.AUTH_DATA_PREFERENCES_NAME,null)
        return if (authDataJson != null) {
            gson.fromJson(authDataJson, AuthData::class.java)
        } else {
            AuthData("")
        }
    }

    fun storeCredentials(authData: AuthData) {

        if (sharedPreferences != null) {
            with (sharedPreferences.edit()) {
                val authDataJson = gson.toJson(authData)
                putString(Preferences.AUTH_DATA_PREFERENCES_NAME, authDataJson)
                commit()
            }
        }
    }

    fun removeCredentials() {
        sharedPreferences?.edit()?.remove(Preferences.AUTH_DATA_PREFERENCES_NAME)?.apply()
    }

    fun isLoggedIn(): Boolean {

        val userData = getUserData()
        val authData = getCredentials()
        return userData.isLoggedIn && authData.token.isNotEmpty()
    }

    fun getUserData(): UserData {

        val userDataJson = sharedPreferences?.getString(Preferences.USER_DATA_PREFERENCES_NAME,null)
        return if (userDataJson != null) {
            gson.fromJson(userDataJson, UserData::class.java)
        } else {
            UserData("", "", false)
        }
    }

    fun storeUserData(userData: UserData) {

        if (sharedPreferences != null) {
            with (sharedPreferences.edit()) {
                val userDataJson = gson.toJson(userData)
                putString(Preferences.USER_DATA_PREFERENCES_NAME, userDataJson)
                commit()
            }
        }
    }

    fun storePassword(password: String) {

        val userData = getUserData()
        userData.password = password
        storeUserData(userData)
    }

    fun removeUserData() {
        sharedPreferences?.edit()?.remove(Preferences.USER_DATA_PREFERENCES_NAME)?.apply()
    }

    fun removePassword() {

        val userData = getUserData()
        userData.password = ""
        userData.isLoggedIn = false
        storeUserData(userData)
    }

    fun getSortParam(): String? {
        return sharedPreferences?.getString(Preferences.SORT_PARAM_PREFERENCE_NAME, null)
    }

    fun setSortParam(sortParam: String?) {

        if (sharedPreferences != null) {
            with (sharedPreferences.edit()) {

                putString(Preferences.SORT_PARAM_PREFERENCE_NAME, sortParam)
                commit()
            }
        }
    }

    fun getVersion(): Int {
        return sharedPreferences?.getInt(Preferences.VERSION_PREFERENCE_NAME, 0) ?: 0
    }

    fun setVersion(version: Int) {

        sharedPreferences?.let {
            with(it.edit()) {
                putInt(Preferences.VERSION_PREFERENCE_NAME, version)
                commit()
            }
        }
    }

    fun getThemeMode(): Int {
        return sharedPreferences?.getInt(Preferences.THEME_MODE_PREFERENCE_NAME, 0) ?: 0
    }

    fun setThemeMode(themeMode: Int) {

        sharedPreferences?.let {
            with(it.edit()) {
                putInt(Preferences.THEME_MODE_PREFERENCE_NAME, themeMode)
                commit()
            }
        }
    }

    fun isSortDescending(): Boolean {
        return sharedPreferences?.getBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, false) ?: false
    }

    fun setIsSortDescending(isSortDescending: Boolean) {

        sharedPreferences?.let {
            with(it.edit()) {
                putBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, isSortDescending)
                commit()
            }
        }
    }

    fun getDateFormatToShow(): String {

        return when (getLanguage()) {
            "es" -> "d MMMM yyyy"
            else -> "MMMM d, yyyy"
        }
    }
}