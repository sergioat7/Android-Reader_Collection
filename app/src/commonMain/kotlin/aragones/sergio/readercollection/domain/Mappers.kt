/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.model.BookResponse
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.data.remote.model.STATES
import aragones.sergio.readercollection.data.remote.model.UserResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.User
import com.aragones.sergio.model.Book as BookLocal
import com.aragones.sergio.util.extensions.toLocalDate
import com.aragones.sergio.util.extensions.toLong

fun Book.toLocalData(): BookLocal = BookLocal(
    id = id,
    title = title,
    subtitle = subtitle,
    authors = authors,
    publisher = publisher,
    publishedDate = publishedDate?.toLong(),
    readingDate = readingDate?.toLong(),
    description = description,
    summary = summary,
    isbn = isbn,
    pageCount = pageCount,
    categories = categories
        ?.joinToString(" / ")
        ?.split("/")
        ?.map { it.trim() }
        ?.distinct(),
    averageRating = averageRating,
    ratingsCount = ratingsCount,
    rating = rating,
    thumbnail = thumbnail,
    image = image,
    format = format,
    state = state,
    priority = priority,
)

fun Book.toRemoteData(): BookResponse = BookResponse(
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
    categories = categories
        ?.joinToString(" / ")
        ?.split("/")
        ?.map { it.trim() }
        ?.distinct(),
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
    publishedDate = publishedDate?.toLocalDate(),
    readingDate = readingDate?.toLocalDate(),
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

fun BookResponse.toDomain(): Book = Book(
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
    categories = volumeInfo.categories
        ?.joinToString(" / ")
        ?.split("/")
        ?.map { it.trim() }
        ?.distinct(),
    averageRating = volumeInfo.averageRating ?: 0.0,
    ratingsCount = volumeInfo.ratingsCount ?: 0,
    rating = 0.0,
    thumbnail = getGoogleBookThumbnail(),
    image = getGoogleBookImage(),
    format = FORMATS.firstOrNull()?.id,
    state = STATES.firstOrNull()?.id,
    priority = -1,
)

fun UserResponse.toDomain(): User = User(
    id = id,
    username = username,
    status = status,
)

fun User.toRemoteData(): UserResponse = UserResponse(
    id = id,
    username = username,
    status = status,
)