/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.utils

import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.toLocalDate
import com.aragones.sergio.util.extensions.toString
import kotlinx.datetime.LocalDate
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import platform.Foundation.preferredLanguages
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.timeZoneWithAbbreviation

actual object UiDateMapper {

    actual fun List<LocalDate>.getGroupedBy(
        pattern: String,
        language: String,
    ): Map<String, List<Any>> {
        val locale = NSLocale(language)
        val simpleDateFormat = NSDateFormatter().apply {
            this.dateFormat = pattern
            this.locale = locale
        }
        return this.mapNotNull { it.toString(format = null).toDate() }.groupBy {
            simpleDateFormat.stringFromDate(it)
        }
    }

    actual fun LocalDate?.getValueToShow(language: String): String? = this
        ?.toString(format = null)
        .toDate()
        .toString(
            format = Constants.getDateFormatToShow(language),
            language = language,
        )

    actual fun String.toLong(language: String?): Long? {
        val date = this
            .toDate(
                format = language?.let { Constants.getDateFormatToShow(it) },
                language = language,
                timeZone = NSTimeZone.timeZoneWithAbbreviation("UTC"),
            )
        return date?.let {
            (it.timeIntervalSince1970 * 1000).toLong()
        }
    }

    actual fun Long.toLocalDate(language: String): LocalDate? = NSDate
        .dateWithTimeIntervalSince1970(this / 1000.0)
        .toString(format = null)
        .toLocalDate()

    actual fun Int.toMonthName(language: String): String {
        val formatter = NSDateFormatter()
        formatter.setLocale(NSLocale(language))
        return formatter.monthSymbols[this - 1].toString().replaceFirstChar { it.uppercase() } + ","
    }

    private fun String?.toDate(
        format: String? = null,
        language: String? = null,
        timeZone: NSTimeZone? = null,
    ): NSDate? {
        val dateFormat = format ?: Constants.DATE_FORMAT
        val locale = language ?: run {
            (NSLocale.preferredLanguages.first() as String).split("-").first()
        }

        val simpleDateFormat = NSDateFormatter().apply {
            this.dateFormat = dateFormat
            this.locale = NSLocale(locale)
            this.timeZone = timeZone ?: NSTimeZone.localTimeZone
        }

        return this?.let {
            try {
                simpleDateFormat.dateFromString(it)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun NSDate?.toString(format: String? = null, language: String? = null): String? {
        val dateFormat = format ?: Constants.DATE_FORMAT
        val locale = language ?: run {
            (NSLocale.preferredLanguages.first() as String).split("-").first()
        }

        val simpleDateFormat = NSDateFormatter().apply {
            this.dateFormat = dateFormat
            this.locale = NSLocale(locale)
            this.timeZone = NSTimeZone.localTimeZone
        }

        return this?.let {
            try {
                simpleDateFormat.stringFromDate(it)
            } catch (_: Exception) {
                null
            }
        }
    }
}