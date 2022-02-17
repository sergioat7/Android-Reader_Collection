/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import aragones.sergio.readercollection.ReaderCollectionApplication
import aragones.sergio.readercollection.models.login.AuthData
import aragones.sergio.readercollection.models.login.UserData
import com.google.gson.Gson
import java.util.*

object SharedPreferencesHandler {

    //region Private properties
    private val appPreferences = ReaderCollectionApplication.context.getSharedPreferences(
        Preferences.PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val appEncryptedPreferences = EncryptedSharedPreferences.create(
        Preferences.ENCRYPTED_PREFERENCES_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        ReaderCollectionApplication.context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val gson = Gson()
    //endregion

    //region Public methods
    fun getLanguage(): String {

        appPreferences.getString(Preferences.LANGUAGE_PREFERENCE_NAME, null)?.let {
            return it
        } ?: run {

            val locale = Locale.getDefault().language
            setLanguage(locale)
            return locale
        }
    }

    fun setLanguage(language: String) {

        with(appPreferences.edit()) {
            putString(Preferences.LANGUAGE_PREFERENCE_NAME, language)
            commit()
        }
    }

    fun getCredentials(): AuthData {

        val authDataJson =
            appEncryptedPreferences.getString(Preferences.AUTH_DATA_PREFERENCES_NAME, null)
        return if (authDataJson != null) {
            gson.fromJson(authDataJson, AuthData::class.java)
        } else {
            AuthData("")
        }
    }

    fun storeCredentials(authData: AuthData) {

        with(appEncryptedPreferences.edit()) {
            val authDataJson = gson.toJson(authData)
            putString(Preferences.AUTH_DATA_PREFERENCES_NAME, authDataJson)
            commit()
        }
    }

    fun removeCredentials() {
        appEncryptedPreferences.edit()?.remove(Preferences.AUTH_DATA_PREFERENCES_NAME)?.apply()
    }

    fun isLoggedIn(): Boolean {

        val userData = getUserData()
        val authData = getCredentials()
        return userData.isLoggedIn && authData.token.isNotEmpty()
    }

    fun getUserData(): UserData {

        val userDataJson =
            appEncryptedPreferences.getString(Preferences.USER_DATA_PREFERENCES_NAME, null)
        return if (userDataJson != null) {
            gson.fromJson(userDataJson, UserData::class.java)
        } else {
            UserData("", "", false)
        }
    }

    fun storeUserData(userData: UserData) {

        with(appEncryptedPreferences.edit()) {
            val userDataJson = gson.toJson(userData)
            putString(Preferences.USER_DATA_PREFERENCES_NAME, userDataJson)
            commit()
        }
    }

    fun storePassword(password: String) {

        val userData = getUserData()
        userData.password = password
        storeUserData(userData)
    }

    fun removeUserData() {
        appEncryptedPreferences.edit()?.remove(Preferences.USER_DATA_PREFERENCES_NAME)?.apply()
    }

    fun removePassword() {

        val userData = getUserData()
        userData.password = ""
        userData.isLoggedIn = false
        storeUserData(userData)
    }

    fun getSortParam(): String? {
        return appPreferences.getString(Preferences.SORT_PARAM_PREFERENCE_NAME, null)
    }

    fun setSortParam(sortParam: String?) {

        with(appPreferences.edit()) {
            putString(Preferences.SORT_PARAM_PREFERENCE_NAME, sortParam)
            commit()
        }
    }

    fun getVersion(): Int {
        return appPreferences.getInt(Preferences.VERSION_PREFERENCE_NAME, 0)
    }

    fun setVersion(version: Int) {

        with(appPreferences.edit()) {
            putInt(Preferences.VERSION_PREFERENCE_NAME, version)
            commit()
        }
    }

    fun getThemeMode(): Int {
        return appPreferences.getInt(Preferences.THEME_MODE_PREFERENCE_NAME, 0)
    }

    fun setThemeMode(themeMode: Int) {

        with(appPreferences.edit()) {
            putInt(Preferences.THEME_MODE_PREFERENCE_NAME, themeMode)
            commit()
        }
    }

    fun isSortDescending(): Boolean {
        return appPreferences.getBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, false)
    }

    fun setIsSortDescending(isSortDescending: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.SORT_ORDER_PREFERENCE_NAME, isSortDescending)
            commit()
        }
    }

    fun getDateFormatToShow(): String {

        return when (getLanguage()) {
            "es" -> "d MMMM yyyy"
            else -> "MMMM d, yyyy"
        }
    }

    fun hasBooksTutorialBeenShown(): Boolean {
        return appPreferences.getBoolean(Preferences.BOOKS_TUTORIAL_PREFERENCE_NAME, false)
    }

    fun setHasBooksTutorialBeenShown(hasBooksTutorialBeenShown: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.BOOKS_TUTORIAL_PREFERENCE_NAME, hasBooksTutorialBeenShown)
            commit()
        }
    }

    fun hasSearchTutorialBeenShown(): Boolean {
        return appPreferences.getBoolean(Preferences.SEARCH_TUTORIAL_PREFERENCE_NAME, false)
    }

    fun setHasSearchTutorialBeenShown(hasSearchTutorialBeenShown: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.SEARCH_TUTORIAL_PREFERENCE_NAME, hasSearchTutorialBeenShown)
            commit()
        }
    }

    fun hasSettingsTutorialBeenShown(): Boolean {
        return appPreferences.getBoolean(Preferences.SETTINGS_TUTORIAL_PREFERENCE_NAME, false)
    }

    fun setHasSettingsTutorialBeenShown(hasSettingsTutorialBeenShown: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.SETTINGS_TUTORIAL_PREFERENCE_NAME, hasSettingsTutorialBeenShown)
            commit()
        }
    }

    fun hasNewBookTutorialBeenShown(): Boolean {
        return appPreferences.getBoolean(Preferences.NEW_BOOK_TUTORIAL_PREFERENCE_NAME, false)
    }

    fun setHasNewBookTutorialBeenShown(hasNewBookTutorialBeenShown: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.NEW_BOOK_TUTORIAL_PREFERENCE_NAME, hasNewBookTutorialBeenShown)
            commit()
        }
    }

    fun hasBookDetailsTutorialBeenShown(): Boolean {
        return appPreferences.getBoolean(Preferences.BOOK_DETAILS_TUTORIAL_PREFERENCE_NAME, false)
    }

    fun setHasBookDetailsTutorialBeenShown(hasBookDetailsTutorialBeenShown: Boolean) {

        with(appPreferences.edit()) {
            putBoolean(Preferences.BOOK_DETAILS_TUTORIAL_PREFERENCE_NAME, hasBookDetailsTutorialBeenShown)
            commit()
        }
    }
}