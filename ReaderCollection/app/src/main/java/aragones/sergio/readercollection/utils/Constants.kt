/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import androidx.fragment.app.FragmentActivity
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.FormatResponse
import aragones.sergio.readercollection.models.StateResponse
import com.getkeepsafe.taptargetview.TapTarget
import com.google.android.material.bottomnavigation.BottomNavigationView

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
    const val BOOKS_TO_SHOW = 7

    var FORMATS = listOf(
        FormatResponse("DIGITAL", "Digital"),
        FormatResponse("PHYSICAL", "Physical")
    )
    var STATES = listOf(
        StateResponse("PENDING", "Pending"),
        StateResponse("READ", "Read"),
        StateResponse("READING", "Reading")
    )

    fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 3
    }

    fun createTargetForBottomNavigationView(
        activity: FragmentActivity?,
        id: Int,
        title: String,
        description: String?
    ): TapTarget {
        return TapTarget.forView(
            activity?.findViewById<BottomNavigationView>(R.id.nav_view)?.findViewById(id),
            title,
            description
        )
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
