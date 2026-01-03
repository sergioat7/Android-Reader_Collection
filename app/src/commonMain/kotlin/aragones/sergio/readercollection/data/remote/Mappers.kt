/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2026
 */

package aragones.sergio.readercollection.data.remote

import aragones.sergio.readercollection.data.remote.model.BookResponse
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalTime::class)
fun Map<String, Any?>.toBook(id: String): BookResponse? = try {
    BookResponse(
        id = id,
        title = getValue("title") as? String,
        subtitle = getValue("subtitle") as? String,
        authors = getValue("authors") as? List<String>,
        publisher = getValue("publisher") as? String,
        publishedDate = (getValue("publishedDate") as? Instant)
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date,
        readingDate = (getValue("readingDate") as? Instant)
            ?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.date,
        description = getValue("description") as? String,
        summary = getValue("summary") as? String,
        isbn = getValue("isbn") as? String,
        pageCount = (getValue("pageCount") as? Number)?.toInt() ?: 0,
        categories = getValue("categories") as? List<String>,
        averageRating = (getValue("averageRating") as? Double) ?: 0.0,
        ratingsCount = (getValue("ratingsCount") as? Number)?.toInt() ?: 0,
        rating = (getValue("rating") as? Double) ?: 0.0,
        thumbnail = getValue("thumbnail") as? String,
        image = getValue("image") as? String,
        format = getValue("format") as? String,
        state = getValue("state") as? String,
        priority = (getValue("priority") as? Number)?.toInt() ?: -1,
    )
} catch (_: Exception) {
    null
}

@OptIn(ExperimentalTime::class)
fun BookResponse.toMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "title" to title,
    "subtitle" to subtitle,
    "authors" to authors,
    "publisher" to publisher,
    "publishedDate" to publishedDate?.atStartOfDayIn(TimeZone.currentSystemDefault()),
    "readingDate" to readingDate?.atStartOfDayIn(TimeZone.currentSystemDefault()),
    "description" to description,
    "summary" to summary,
    "isbn" to isbn,
    "pageCount" to pageCount,
    "categories" to categories,
    "averageRating" to averageRating,
    "ratingsCount" to ratingsCount,
    "rating" to rating,
    "thumbnail" to thumbnail,
    "image" to image,
    "format" to format,
    "state" to state,
    "priority" to priority,
)