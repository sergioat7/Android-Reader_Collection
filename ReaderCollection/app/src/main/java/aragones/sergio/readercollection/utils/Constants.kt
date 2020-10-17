/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/10/2020
 */

package aragones.sergio.readercollection.utils

import android.annotation.SuppressLint
import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class Constants {
    companion object {

        // MARK: - Retrofit constants

        const val BASE_ENDPOINT = "https://books-collection-services.herokuapp.com/"
        const val BASE_GOOGLE_ENDPOINT = "https://www.googleapis.com/books/v1/"
        const val ACCEPT_LANGUAGE_HEADER = "Accept-Language"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val CONNECT_TIMEOUT: Long = 60
        const val READ_TIMEOUT: Long = 30
        const val WRITE_TIMEOUT: Long = 15
        val SUBSCRIBER_SCHEDULER = Schedulers.io()
        val OBSERVER_SCHEDULER = AndroidSchedulers.mainThread()

        // MARK: - SharedPref constants

        const val PREFERENCES_NAME = "preferences"
        const val AUTH_DATA_PREFERENCES_NAME = "authData"
        const val LANGUAGE_PREFERENCE_NAME = "language"

        // MARK: Date format

        const val DATE_FORMAT = "yyyy-MM-dd"

        fun getDateFormatToShow(sharedPrefHandler: SharedPreferencesHandler): String {
            return if (sharedPrefHandler.getLanguage() == "es") "d MMMM, yyyy" else "MMMM d, yyyy"
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToString(date: Date?): String? {
            date?.let {

                return try {
                    SimpleDateFormat(DATE_FORMAT).format(it)
                } catch (e: Exception) {

                    Log.e("Constants", e.message)
                    null
                }
            } ?: run {

                Log.e("Constants", "date null")
                return null
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun stringToDate(dateString: String?): Date? {
            dateString?.let {

                return try {
                    SimpleDateFormat(DATE_FORMAT).parse(it)
                } catch (e: Exception) {

                    Log.e("Constants", e.message)
                    null
                }
            } ?: run {
                Log.e("Constants", "dateString null")
                return null
            }
        }
    }
}
