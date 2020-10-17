/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import aragones.sergio.readercollection.models.AuthData
import com.google.gson.Gson
import java.util.*

class SharedPreferencesHandler(context: Context?) {

    //MARK: Private properties

    private val sharedPreferences = context?.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    //MARK: Public methods

    fun getLanguage(): String {

        sharedPreferences?.getString(Constants.LANGUAGE_PREFERENCE_NAME, null)?.let {
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

                putString(Constants.LANGUAGE_PREFERENCE_NAME, language)
                commit()
            }
        }
    }

    fun getCredentials(): AuthData {

        val authDataJson = sharedPreferences?.getString(Constants.AUTH_DATA_PREFERENCES_NAME,null)
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
                putString(Constants.AUTH_DATA_PREFERENCES_NAME, authDataJson)
                commit()
            }
        }
    }

    fun removeCredentials() {
        sharedPreferences?.edit()?.remove(Constants.AUTH_DATA_PREFERENCES_NAME)?.apply()
    }
}