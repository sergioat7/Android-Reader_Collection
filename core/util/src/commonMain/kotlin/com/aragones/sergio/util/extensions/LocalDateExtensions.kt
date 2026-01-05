/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/1/2026
 */

package com.aragones.sergio.util.extensions

import com.aragones.sergio.util.Constants
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

@OptIn(FormatStringsInDatetimeFormats::class)
fun LocalDate?.toString(format: String? = null): String? {
    val dateFormat = format ?: Constants.DATE_FORMAT
    val customFormat = LocalDate.Format { byUnicodePattern(dateFormat) }
    return this?.let {
        try {
            it.format(customFormat)
        } catch (_: Exception) {
            null
        }
    }
}

@OptIn(ExperimentalTime::class)
fun LocalDate?.toLong(): Long? =
    this?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toEpochMilliseconds()

@OptIn(ExperimentalTime::class)
fun currentLocalDate(): LocalDate = Clock.System
    .now()
    .toLocalDateTime(TimeZone.currentSystemDefault())
    .date

@OptIn(ExperimentalTime::class)
fun currentTime(): Long = Clock.System.now().toEpochMilliseconds()

fun LocalDate?.getYear(): Int = this?.year ?: currentLocalDate().year

fun LocalDate?.getMonthNumber(): Int = this?.month?.number ?: currentLocalDate().month.number