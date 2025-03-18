/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.utils.Constants.FORMATS
import aragones.sergio.readercollection.utils.Constants.STATES
import com.aragones.sergio.model.Book as BookLocal

fun Book.toLocalData(): BookLocal = BookLocal(
    id = id,
    title = title,
    subtitle = subtitle,
    authors = authors,
    publisher = publisher,
    publishedDate = publishedDate,
    readingDate = readingDate,
    description = description,
    summary = summary,
    isbn = isbn,
    pageCount = pageCount,
    categories = categories,
    averageRating = averageRating,
    ratingsCount = ratingsCount,
    rating = rating,
    thumbnail = thumbnail,
    image = image,
    format = format,
    state = state,
    priority = priority,
)

fun BookLocal.toDomain(): Book = Book(
    id = id,
    title = title,
    subtitle = subtitle,
    authors = authors,
    publisher = publisher,
    publishedDate = publishedDate,
    readingDate = readingDate,
    description = description,
    summary = summary,
    isbn = isbn,
    pageCount = pageCount,
    categories = categories,
    averageRating = averageRating,
    ratingsCount = ratingsCount,
    rating = rating,
    thumbnail = thumbnail,
    image = image,
    format = format,
    state = state,
    priority = priority,
)

fun GoogleBookResponse.toDomain(): Book = Book(
    id = id,
    title = StringBuilder()
        .append(volumeInfo.title ?: "")
        .append(" ")
        .append(volumeInfo.subtitle ?: "")
        .toString(),
    subtitle = null,
    authors = volumeInfo.authors,
    publisher = volumeInfo.publisher,
    publishedDate = volumeInfo.publishedDate,
    readingDate = null,
    description = volumeInfo.description,
    summary = null,
    isbn = getGoogleBookIsbn(),
    pageCount = volumeInfo.pageCount ?: 0,
    categories = volumeInfo.categories,
    averageRating = volumeInfo.averageRating ?: 0.0,
    ratingsCount = volumeInfo.ratingsCount ?: 0,
    rating = 0.0,
    thumbnail = getGoogleBookThumbnail(),
    image = getGoogleBookImage(),
    format = FORMATS.firstOrNull()?.id,
    state = STATES.firstOrNull()?.id,
    priority = -1,
)