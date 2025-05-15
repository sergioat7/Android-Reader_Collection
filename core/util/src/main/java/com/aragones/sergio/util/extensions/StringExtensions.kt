/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package com.aragones.sergio.util.extensions

import com.aragones.sergio.util.Constants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun String?.toList(): List<String> = if (this != null && this.isNotBlank()) {
    this
        .trimStart()
        .trimEnd()
        .split(",")
        .toList()
} else {
    ArrayList()
}

fun String?.toDate(
    format: String? = null,
    language: String? = null,
    timeZone: TimeZone? = null,
): Date? {
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
            null
        }
    } ?: run {
        return null
    }
}

fun String?.isNotBlank(): Boolean = !this.isNullOrBlank()