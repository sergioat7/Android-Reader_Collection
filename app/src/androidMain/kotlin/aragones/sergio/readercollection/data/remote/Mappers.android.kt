/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/1/2026
 */

package aragones.sergio.readercollection.data.remote

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
actual fun Any?.fromNativeDate(): LocalDate? = (this as? Instant)
    ?.toLocalDateTime(TimeZone.currentSystemDefault())
    ?.date

@OptIn(ExperimentalTime::class)
actual fun LocalDate?.toNativeDate(): Any? = this?.atStartOfDayIn(TimeZone.currentSystemDefault())