/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.domain

import aragones.sergio.readercollection.data.remote.model.GoogleBookResponse
import aragones.sergio.readercollection.domain.model.Book
import com.aragones.sergio.model.Book as BookLocal

fun Book.toLocalData(): BookLocal {
    return BookLocal(
        id,
        title,
        subtitle,
        authors,
        publisher,
        publishedDate,
        readingDate,
        description,
        summary,
        isbn,
        pageCount,
        categories,
        averageRating,
        ratingsCount,
        rating,
        thumbnail,
        image,
        format,
        state,
        isFavourite,
        priority
    )
}

fun BookLocal.toDomain(): Book {
    return Book(
        id,
        title,
        subtitle,
        authors,
        publisher,
        publishedDate,
        readingDate,
        description,
        summary,
        isbn,
        pageCount,
        categories,
        averageRating,
        ratingsCount,
        rating,
        thumbnail,
        image,
        format,
        state,
        isFavourite,
        priority
    )
}

fun GoogleBookResponse.toDomain(): Book {
    return Book(
        id = id,
        title = StringBuilder().append(volumeInfo.title ?: "").append(" ")
            .append(volumeInfo.subtitle ?: "").toString(),
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
        format = null,
        state = null,
        isFavourite = false,
        priority = -1
    )
}