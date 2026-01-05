/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 2/1/2022
 */

package com.aragones.sergio.util.extensions

import com.aragones.sergio.util.Constants
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern

@OptIn(FormatStringsInDatetimeFormats::class)
fun String?.toLocalDate(format: String? = null): LocalDate? {
    val dateFormat = format ?: Constants.DATE_FORMAT
    val customFormat = LocalDate.Format { byUnicodePattern(dateFormat) }
    return this?.let {
        try {
            LocalDate.parse(it, customFormat)
        } catch (_: Exception) {
            null
        }
    }
}

fun String?.isNotBlank(): Boolean = !this.isNullOrBlank()