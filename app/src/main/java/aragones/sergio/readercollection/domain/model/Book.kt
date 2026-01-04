/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/3/2024
 */

package aragones.sergio.readercollection.domain.model

import androidx.compose.runtime.Immutable
import androidx.room.Ignore
import aragones.sergio.readercollection.data.remote.DateSerializer
import com.aragones.sergio.util.BookState
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Book(
    @SerialName("googleId")
    val id: String,
    @SerialName("title")
    val title: String? = null,
    @SerialName("subtitle")
    val subtitle: String? = null,
    @SerialName("authors")
    val authors: List<String>? = null,
    @SerialName("publisher")
    val publisher: String? = null,
    @SerialName("publishedDate")
    @Serializable(with = DateSerializer::class)
    val publishedDate: LocalDate? = null,
    @SerialName("readingDate")
    @Serializable(with = DateSerializer::class)
    val readingDate: LocalDate? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("summary")
    val summary: String? = null,
    @SerialName("isbn")
    val isbn: String? = null,
    @SerialName("pageCount")
    val pageCount: Int,
    @SerialName("categories")
    val categories: List<String>? = null,
    @SerialName("averageRating")
    val averageRating: Double,
    @SerialName("ratingsCount")
    val ratingsCount: Int,
    @SerialName("rating")
    val rating: Double,
    @SerialName("thumbnail")
    val thumbnail: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("format")
    val format: String? = null,
    @SerialName("state")
    var state: String? = null,
    var priority: Int,
) {

    @Ignore
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