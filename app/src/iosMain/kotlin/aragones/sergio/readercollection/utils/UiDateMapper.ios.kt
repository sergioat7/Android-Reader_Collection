/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.utils

import kotlinx.datetime.LocalDate

actual object UiDateMapper {

    actual fun List<LocalDate>.getGroupedBy(
        pattern: String,
        language: String,
    ): Map<String, List<Any>> {
        TODO("Not yet implemented")
    }

    actual fun LocalDate?.getValueToShow(language: String): String? {
        TODO("Not yet implemented")
    }

    actual fun String.toLong(language: String?): Long? {
        TODO("Not yet implemented")
    }

    actual fun Long.toLocalDate(language: String): LocalDate? {
        TODO("Not yet implemented")
    }

    actual fun Int.toMonthName(language: String): String {
        TODO("Not yet implemented")
    }
}