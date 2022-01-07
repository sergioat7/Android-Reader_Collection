/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package aragones.sergio.readercollection.extensions

import android.util.Log
import aragones.sergio.readercollection.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

fun Date?.toString(format: String? = null, language: String? = null): String? {

    val dateFormat = format ?: Constants.DATE_FORMAT
    val locale = language?.let {
        Locale.forLanguageTag(it)
    } ?: run {
        Locale.getDefault()
    }
    this?.let {

        return try {
            SimpleDateFormat(dateFormat, locale).format(it)
        } catch (e: Exception) {

            Log.e("DateExtensions", e.message ?: "")
            null
        }
    } ?: run {

        Log.e("DateExtensions", "date null")
        return null
    }
}