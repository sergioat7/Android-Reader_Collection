/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2026
 */

package aragones.sergio.readercollection.data.remote

import kotlin.time.ExperimentalTime
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toKotlinInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toNSDate
import platform.Foundation.NSDate

@OptIn(ExperimentalTime::class)
actual fun Any?.fromNativeDate(): LocalDate? = (this as? NSDate)
    ?.toKotlinInstant()
    ?.toLocalDateTime(TimeZone.currentSystemDefault())
    ?.date

@OptIn(ExperimentalTime::class)
actual fun LocalDate?.toNativeDate(): Any? =
    this?.atStartOfDayIn(TimeZone.currentSystemDefault())?.toNSDate()