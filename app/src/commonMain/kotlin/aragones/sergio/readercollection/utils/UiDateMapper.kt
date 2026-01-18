/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2026
 */

package aragones.sergio.readercollection.utils

import kotlinx.datetime.LocalDate

expect object UiDateMapper {
    fun List<LocalDate>.getGroupedBy(pattern: String, language: String): Map<String, List<Any>>
    fun LocalDate?.getValueToShow(language: String): String?
    fun String.toLong(language: String?): Long?
    fun Long.toLocalDate(language: String): LocalDate?
    fun Int.toMonthName(language: String): String
}