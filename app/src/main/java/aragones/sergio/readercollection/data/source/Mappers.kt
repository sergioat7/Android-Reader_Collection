/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/3/2024
 */

package aragones.sergio.readercollection.data.source

import com.aragones.sergio.data.business.BookResponse
import com.aragones.sergio.model.Book

fun BookResponse.toLocalData(): Book {
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

fun Book.toDomain(): BookResponse {
    return BookResponse(
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