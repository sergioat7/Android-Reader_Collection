/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.adapters.SpinnerAdapter
import aragones.sergio.readercollection.models.responses.*
import aragones.sergio.readercollection.network.apiclient.APIClient
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.schedulers.Schedulers
import retrofit2.HttpException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class Constants {
    companion object {

        fun isDarkMode(context: Context?): Boolean {

            val mode = context?.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
            return mode == Configuration.UI_MODE_NIGHT_YES
        }

        fun<T> listToString(list: List<T>?): String {

            var result = StringBuilder()
            list?.let {
                for (element in it) {

                    result.append(element.toString())
                    result.append(", ")
                }
                result = StringBuilder(if (result.isEmpty()) "" else result.substring(0, result.length - 2))
            }
            return result.toString()
        }

        // MARK: - Retrofit constants

        const val BASE_ENDPOINT = "https://books-collection-services.herokuapp.com/"
        const val BASE_GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/"
        const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val CONNECT_TIMEOUT: Long = 60
        const val READ_TIMEOUT: Long = 30
        const val WRITE_TIMEOUT: Long = 15
        const val FORMAT_PARAM = "format"
        const val STATE_PARAM = "state"
        const val IS_FAVOURITE_PARAM = "isFavourite"
        const val SEARCH_PARAM = "q"
        const val PAGE_PARAM = "startIndex"
        const val RESULTS_PARAM = "maxResults"
        const val ORDER_PARAM = "orderBy"
        const val RESULTS = 20
        const val RELEVANCE_ORDER = "relevance "
        const val NEWEST_ORDER = "newest"
        val SUBSCRIBER_SCHEDULER: Scheduler = Schedulers.io()
        val OBSERVER_SCHEDULER: Scheduler = AndroidSchedulers.mainThread()

        fun handleError(error: Throwable): ErrorResponse {

            lateinit var errorResponse: ErrorResponse
            if (error is HttpException) {
                error.response()?.errorBody()?.let { errorBody ->

                    errorResponse = APIClient.gson.fromJson(
                        errorBody.charStream(), ErrorResponse::class.java
                    )
                } ?: run {
                    errorResponse = ErrorResponse("", R.string.error_server)
                }
            } else {
                errorResponse = ErrorResponse("", R.string.error_server)
            }
            return errorResponse
        }

        // MARK: - SharedPref constants

        const val PREFERENCES_NAME = "preferences"
        const val LANGUAGE_PREFERENCE_NAME = "language"
        const val AUTH_DATA_PREFERENCES_NAME = "authData"
        const val USER_DATA_PREFERENCES_NAME = "userData"
        const val ENGLISH_LANGUAGE_KEY = "en"
        const val SPANISH_LANGUAGE_KEY = "es"

        // MARK: Date format

        const val DATE_FORMAT = "yyyy-MM-dd"

        fun getDateFormatToShow(sharedPrefHandler: SharedPreferencesHandler): String {
            return if (sharedPrefHandler.getLanguage() == "es") "d MMMM yyyy" else "MMMM d, yyyy"
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToString(date: Date?, format: String? = null): String? {

            val dateFormat = format ?: DATE_FORMAT
            date?.let {

                return try {
                    SimpleDateFormat(dateFormat).format(it)
                } catch (e: Exception) {

                    Log.e("Constants", e.message ?: "")
                    null
                }
            } ?: run {

                Log.e("Constants", "date null")
                return null
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun stringToDate(dateString: String?, format: String? = null): Date? {

            val dateFormat = format ?: DATE_FORMAT
            dateString?.let {

                return try {
                    SimpleDateFormat(dateFormat).parse(it)
                } catch (e: Exception) {

                    Log.e("Constants", e.message ?: "")
                    null
                }
            } ?: run {
                Log.e("Constants", "dateString null")
                return null
            }
        }

        // MARK: - Login constants

        fun isUserNameValid(username: String): Boolean {
            return username.isNotBlank()
        }

        fun isPasswordValid(password: String): Boolean {
            return password.length > 3
        }

        fun showOrHidePassword(editText: EditText, imageButton: ImageButton, isDarkMode: Boolean) {

            if (editText.transformationMethod is HideReturnsTransformationMethod) {

                val image = if (isDarkMode) R.drawable.ic_show_password_dark else R.drawable.ic_show_password_light
                imageButton.setImageResource(image)
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
            } else {

                val image = if (isDarkMode) R.drawable.ic_hide_password_dark else R.drawable.ic_hide_password_light
                imageButton.setImageResource(image)
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            }
        }

        // MARK: - Search books constants

        fun hideSoftKeyboard(activity: Activity) {

            activity.currentFocus?.let { currentFocus ->

                val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
            } ?: return
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

            return BookResponse(
                googleBook.id,
                googleBook.volumeInfo.title,
                googleBook.volumeInfo.subtitle,
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
                if (it.isNotEmpty()) {
                    return it[0]
                }
            }

            industryIdentifiers?.mapNotNull { if (it.type == "ISBN_10") it.identifier else null }?.let {
                if (it.isNotEmpty()) {
                    return it[0]
                }
            }

            industryIdentifiers?.mapNotNull { if (it.type == "OTHER") it.identifier else null }?.let {
                if (it.isNotEmpty()) {
                    return it[0]
                }
            }

            return null
        }

        private fun getGoogleBookThumbnail(imageLinks: GoogleImageLinksResponse?): String? {
            return imageLinks?.thumbnail ?: imageLinks?.smallThumbnail
        }

        private fun getGoogleBookImage(imageLinks: GoogleImageLinksResponse?): String? {
            return imageLinks?.extraLarge ?: imageLinks?.large ?: imageLinks?.medium ?: imageLinks?.small
        }

        // MARK: - Book detail

        const val BOOK_ID = "bookId"
        const val IS_GOOGLE_BOOK = "isGoogleBook"
        const val MAX_LINES = Int.MAX_VALUE
        const val NO_VALUE = "-"

        fun getFavouriteImage(isFavourite: Boolean, context: Context?): Int {

            return if (isFavourite && isDarkMode(context)) {
                R.drawable.ic_favourite_full_dark
            } else if (isFavourite && !isDarkMode(context)) {
                R.drawable.ic_favourite_full_light
            } else if (!isFavourite && isDarkMode(context)) {
                R.drawable.ic_favourite_empty_dark
            } else {
                R.drawable.ic_favourite_empty_light
            }
        }

        fun getAdapter(context: Context, data: List<String>): SpinnerAdapter {

            val arrayAdapter = SpinnerAdapter(context, data)
            arrayAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            return arrayAdapter
        }
    }
}
