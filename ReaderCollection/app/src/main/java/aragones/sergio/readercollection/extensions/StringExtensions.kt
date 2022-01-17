/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.util.Log
import aragones.sergio.readercollection.utils.Constants
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun <T> String?.toList(): List<T> {

    return if (this != null && this.isNotBlank()) {
        this.trimStart().trimEnd().split(",").toList() as List<T>
    } else {
        ArrayList()
    }
}

fun String?.toDate(format: String? = null, language: String? = null, timeZone: TimeZone? = null): Date? {

    val dateFormat = format ?: Constants.DATE_FORMAT
    val locale = language?.let {
        Locale.forLanguageTag(it)
    } ?: run {
        Locale.getDefault()
    }
    val simpleDateFormat = SimpleDateFormat(dateFormat, locale)
    simpleDateFormat.timeZone = timeZone ?: TimeZone.getDefault()

    this?.let {

        return try {
            simpleDateFormat.parse(it)
        } catch (e: Exception) {

            Log.e("StringExtensions", e.message ?: "")
            null
        }
    } ?: run {
        Log.e("StringExtensions", "dateString null")
        return null
    }
}

fun String?.isNotBlank(): Boolean {
    return !this.isNullOrBlank()
}