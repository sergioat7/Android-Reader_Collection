/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.FormatResponse
import aragones.sergio.readercollection.models.StateResponse

object Preferences {
    const val PREFERENCES_NAME = "preferences"
    const val ENCRYPTED_PREFERENCES_NAME = "encryptedPreferences"
    const val LANGUAGE_PREFERENCE_NAME = "language"
    const val AUTH_DATA_PREFERENCES_NAME = "authData"
    const val USER_DATA_PREFERENCES_NAME = "userData"
    const val ENGLISH_LANGUAGE_KEY = "en"
    const val SPANISH_LANGUAGE_KEY = "es"
    const val SORT_PARAM_PREFERENCE_NAME = "sortParam"
    const val VERSION_PREFERENCE_NAME = "version"
    const val THEME_MODE_PREFERENCE_NAME = "themeMode"
    const val SORT_ORDER_PREFERENCE_NAME = "sortOrder"
}

object Constants {
    const val DATABASE_NAME = "ReaderCollection"
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val MAX_LINES = Int.MAX_VALUE
    const val NO_VALUE = "-"

    var FORMATS = listOf<FormatResponse>()
    var STATES = listOf<StateResponse>()

    fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 3
    }

    fun getRoundedTextView(text: String, context: Context): TextView {

        val tv = TextView(
            context,
            null,
            R.style.Widget_ReaderCollection_RoundedTextView,
            R.style.Widget_ReaderCollection_RoundedTextView
        )
        tv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tv.gravity = Gravity.CENTER
        tv.text = text
        return tv
    }
}

object State {
    const val PENDING = "PENDING"
    const val READING = "READING"
    const val READ = "READ"
}

enum class ScrollPosition {
    TOP, MIDDLE, END
}

enum class StatusBarStyle {
    PRIMARY,
    SECONDARY
}

enum class CustomInputType {
    TEXT,
    MULTI_LINE_TEXT,
    NUMBER,
    PASSWORD,
    DATE
}

enum class CustomDropdownType {
    FORMAT,
    STATE,
    SORT_PARAM,
    SORT_ORDER,
    APP_THEME
}
