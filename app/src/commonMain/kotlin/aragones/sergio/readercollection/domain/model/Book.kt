/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.domain.model

import androidx.compose.runtime.Immutable
import aragones.sergio.readercollection.data.remote.model.GenreResponse
import com.aragones.sergio.util.BookState
import kotlinx.datetime.LocalDate

@Immutable
data class Book(
    val id: String,
    val title: String? = null,
    val subtitle: String? = null,
    val authors: List<String>? = null,
    val publisher: String? = null,
    val publishedDate: LocalDate? = null,
    val readingDate: LocalDate? = null,
    val description: String? = null,
    val summary: String? = null,
    val isbn: String? = null,
    val pageCount: Int,
    val categories: List<GenreResponse>? = null,
    val averageRating: Double,
    val ratingsCount: Int,
    val rating: Double,
    val thumbnail: String? = null,
    val image: String? = null,
    val format: String? = null,
    var state: String? = null,
    var priority: Int,
) {

    constructor(id: String) : this(
        id,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        0,
        null,
        0.0,
        0,
        0.0,
        null,
        null,
        null,
        null,
        -1,
    )

    fun authorsToString(): String = authors?.joinToString(separator = ", ") ?: ""

    fun isPending(): Boolean = state == BookState.PENDING

    fun isReading(): Boolean = state == BookState.READING
}

@Immutable
data class Books(val books: List<Book> = emptyList())