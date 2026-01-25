/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2026
 */

package aragones.sergio.readercollection.utils

import android.os.Build
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.toLocalDate
import com.aragones.sergio.util.extensions.toString
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlinx.datetime.LocalDate

actual object UiDateMapper {

    actual fun List<LocalDate>.getGroupedBy(
        pattern: String,
        language: String,
    ): Map<String, List<Any>> {
        val locale = Locale.forLanguageTag(language)
        val simpleDateFormat = SimpleDateFormat(pattern, locale)
        return this.mapNotNull { it.toString(format = null).toDate() }.groupBy {
            simpleDateFormat.format(it)
        }
    }

    actual fun LocalDate?.getValueToShow(language: String): String? = this
        ?.toString(format = null)
        .toDate()
        .toString(
            format = Constants.getDateFormatToShow(language),
            language = language,
        )

    actual fun String.toLong(language: String?): Long? = this
        .toDate(
            format = language?.let { Constants.getDateFormatToShow(it) },
            language = language,
            timeZone = TimeZone.getTimeZone("UTC"),
        )?.time

    actual fun Long.toLocalDate(language: String): LocalDate? {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = this
        val dateString = calendar.time
            .toString(format = null)
            .toLocalDate()
        return dateString
    }

    actual fun Int.toMonthName(language: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Month
                .of(this)
                .getDisplayName(
                    TextStyle.FULL,
                    Locale.forLanguageTag(language),
                ).lowercase()
                .replaceFirstChar { it.uppercase() } + ","
        } else {
            ""
        }

    private fun String?.toDate(
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

        return this?.let {
            try {
                simpleDateFormat.parse(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun Date?.toString(format: String? = null, language: String? = null): String? {
        val dateFormat = format ?: Constants.DATE_FORMAT
        val locale = language?.let {
            Locale.forLanguageTag(it)
        } ?: run {
            Locale.getDefault()
        }
        return this?.let {
            try {
                SimpleDateFormat(dateFormat, locale).format(it)
            } catch (_: Exception) {
                null
            }
        }
    }
}