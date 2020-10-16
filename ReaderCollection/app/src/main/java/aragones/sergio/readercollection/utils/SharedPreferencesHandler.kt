/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import java.util.*

class SharedPreferencesHandler(context: Context?) {

    private val sharedPreferences = context?.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE)

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
}