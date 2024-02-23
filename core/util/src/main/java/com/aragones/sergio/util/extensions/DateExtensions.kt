/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package com.aragones.sergio.util.extensions

import com.aragones.sergio.util.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
            null
        }
    } ?: run {
        return null
    }
}

fun Date?.getYear(): Int {

    val cal1 = Calendar.getInstance()
    cal1.time = this ?: Date()
    return cal1[Calendar.YEAR]
}

fun Date?.getMonthNumber(): Int {

    val cal1 = Calendar.getInstance()
    cal1.time = this ?: Date()
    return cal1[Calendar.MONTH]
}

fun List<Date>.getOrderedBy(field: Int): List<Date> {

    val calendar = Calendar.getInstance()
    return this.sortedBy {
        calendar.time = it
        calendar.get(field)
    }
}

fun List<Date>.getGroupedBy(pattern: String, language: String): Map<String, List<Date>> {

    val locale = Locale.forLanguageTag(language)
    val calendar = Calendar.getInstance()
    return this.groupBy {
        calendar.time = it
        SimpleDateFormat(pattern, locale).format(calendar.time)
    }
}