/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package com.aragones.sergio.util

object Preferences {
    const val PREFERENCES_NAME = "preferences"
    const val ENCRYPTED_PREFERENCES_NAME = "encryptedPreferences"
    const val LANGUAGE_PREFERENCE_NAME = "language"
    const val AUTH_DATA_PREFERENCES_NAME = "authData"
    const val USER_DATA_PREFERENCES_NAME = "userData"
    const val ENGLISH_LANGUAGE_KEY = "en"
    const val SPANISH_LANGUAGE_KEY = "es"
    const val AUTOMATIC_SYNC_PREFERENCE_NAME = "automaticSync"
    const val SORT_PARAM_PREFERENCE_NAME = "sortParam"
    const val THEME_MODE_PREFERENCE_NAME = "themeMode"
    const val SORT_ORDER_PREFERENCE_NAME = "sortOrder"
    const val BOOKS_TUTORIAL_PREFERENCE_NAME = "booksTutorial"
    const val DRAG_TUTORIAL_PREFERENCE_NAME = "dragTutorial"
    const val SEARCH_TUTORIAL_PREFERENCE_NAME = "searchTutorial"
    const val STATISTICS_TUTORIAL_PREFERENCE_NAME = "statisticsTutorial"
    const val SETTINGS_TUTORIAL_PREFERENCE_NAME = "settingsTutorial"
    const val NEW_BOOK_TUTORIAL_PREFERENCE_NAME = "newBookTutorial"
    const val BOOK_DETAILS_TUTORIAL_PREFERENCE_NAME = "bookDetailsTutorial"
}

object Constants {
    const val DATE_FORMAT = "yyyy-MM-dd"
    const val EMPTY_VALUE = ""
    const val NO_VALUE = "-"
    const val BOOKS_TO_SHOW = 7

    fun isUserNameValid(username: String): Boolean = username.isNotBlank()

    fun isPasswordValid(password: String): Boolean = password.length > 5

    fun getDateFormatToShow(language: String): String = when (language) {
        "es" -> "d MMMM yyyy"
        else -> "MMMM d, yyyy"
    }
}

object BookState {
    const val PENDING = "PENDING"
    const val READING = "READING"
    const val READ = "READ"
}

enum class CustomInputType {
    TEXT,
    MULTI_LINE_TEXT,
    NUMBER,
    PASSWORD,
    DATE,
}
