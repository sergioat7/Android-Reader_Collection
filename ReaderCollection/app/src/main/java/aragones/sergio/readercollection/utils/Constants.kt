/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.GoogleBookResponse
import aragones.sergio.readercollection.models.responses.GoogleImageLinksResponse
import aragones.sergio.readercollection.models.responses.GoogleIsbnResponse

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

    fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    fun isPasswordValid(password: String): Boolean {
        return password.length > 3
    }

    fun getValuePositionInArray(value: String, values: Array<String>): Int {

        var i = 0
        while (i < values.size) {
            if (values[i] == value) {
                break
            }
            i++
        }
        return i % values.size
    }

    fun getRoundedTextView(text: String, context: Context): TextView {

        val tv = TextView(context, null, R.style.RoundedTextView, R.style.RoundedTextView)
        tv.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tv.text = text
        return tv
    }

    fun mapGoogleBooks(googleBooks: List<GoogleBookResponse>?): List<BookResponse> {

        val result = mutableListOf<BookResponse>()
        googleBooks?.let {
            for (googleBook in it) {
                result.add(mapGoogleBook(googleBook))
            }
        }
        return result
    }

    fun mapGoogleBook(googleBook: GoogleBookResponse): BookResponse {

        val title = StringBuilder()
            .append(googleBook.volumeInfo.title ?: "")
            .append(" ")
            .append(googleBook.volumeInfo.subtitle ?: "")
            .toString()
        return BookResponse(
            googleBook.id,
            title,
            null,
            googleBook.volumeInfo.authors,
            googleBook.volumeInfo.publisher,
            googleBook.volumeInfo.publishedDate,
            null,
            googleBook.volumeInfo.description,
            null,
            getGoogleBookIsbn(googleBook.volumeInfo.industryIdentifiers),
            googleBook.volumeInfo.pageCount ?: 0,
            googleBook.volumeInfo.categories,
            googleBook.volumeInfo.averageRating ?: 0.0,
            googleBook.volumeInfo.ratingsCount ?: 0,
            0.0,
            getGoogleBookThumbnail(googleBook.volumeInfo.imageLinks),
            getGoogleBookImage(googleBook.volumeInfo.imageLinks),
            null,
            null,
            false
        )
    }

    private fun getGoogleBookIsbn(industryIdentifiers: List<GoogleIsbnResponse>?): String? {

        industryIdentifiers?.mapNotNull { if (it.type == "ISBN_13") it.identifier else null }?.let {
            if (it.isNotEmpty()) return it[0]
        }
        industryIdentifiers?.mapNotNull { if (it.type == "ISBN_10") it.identifier else null }?.let {
            if (it.isNotEmpty()) return it[0]
        }
        industryIdentifiers?.mapNotNull { if (it.type == "OTHER") it.identifier else null }?.let {
            if (it.isNotEmpty()) return it[0]
        }
        return null
    }

    private fun getGoogleBookThumbnail(imageLinks: GoogleImageLinksResponse?): String? {
        return imageLinks?.thumbnail ?: imageLinks?.smallThumbnail
    }

    private fun getGoogleBookImage(imageLinks: GoogleImageLinksResponse?): String? {
        return imageLinks?.extraLarge ?: imageLinks?.large ?: imageLinks?.medium
        ?: imageLinks?.small
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
