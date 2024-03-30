/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.aragones.sergio.util.State
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
@Entity(tableName = "Book")
data class BookResponse(
    @PrimaryKey
    @Json(name = "googleId")
    override val id: String,
    @Json(name = "title")
    val title: String?,
    @Json(name = "subtitle")
    val subtitle: String?,
    @Json(name = "authors")
    val authors: List<String>?,
    @Json(name = "publisher")
    val publisher: String?,
    @Json(name = "publishedDate")
    val publishedDate: Date?,
    @Json(name = "readingDate")
    val readingDate: Date?,
    @Json(name = "description")
    val description: String?,
    @Json(name = "summary")
    val summary: String?,
    @Json(name = "isbn")
    val isbn: String?,
    @Json(name = "pageCount")
    val pageCount: Int,
    @Json(name = "categories")
    val categories: List<String>?,
    @Json(name = "averageRating")
    val averageRating: Double,
    @Json(name = "ratingsCount")
    val ratingsCount: Int,
    @Json(name = "rating")
    val rating: Double,
    @Json(name = "thumbnail")
    val thumbnail: String?,
    @Json(name = "image")
    val image: String?,
    @Json(name = "format")
    val format: String?,
    @Json(name = "state")
    var state: String?,
    @Json(name = "isFavourite")
    var isFavourite: Boolean,
    var priority: Int
) : BaseModel<String> {

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
        false,
        -1
    )

    @Ignore
    constructor(googleBook: GoogleBookResponse) : this(
        id = googleBook.id,
        title = StringBuilder().append(googleBook.volumeInfo.title ?: "").append(" ")
            .append(googleBook.volumeInfo.subtitle ?: "").toString(),
        subtitle = null,
        authors = googleBook.volumeInfo.authors,
        publisher = googleBook.volumeInfo.publisher,
        publishedDate = googleBook.volumeInfo.publishedDate,
        readingDate = null,
        description = googleBook.volumeInfo.description,
        summary = null,
        isbn = googleBook.getGoogleBookIsbn(),
        pageCount = googleBook.volumeInfo.pageCount ?: 0,
        categories = googleBook.volumeInfo.categories,
        averageRating = googleBook.volumeInfo.averageRating ?: 0.0,
        ratingsCount = googleBook.volumeInfo.ratingsCount ?: 0,
        rating = 0.0,
        thumbnail = googleBook.getGoogleBookThumbnail(),
        image = googleBook.getGoogleBookImage(),
        format = null,
        state = null,
        isFavourite = false,
        priority = -1
    )

    fun authorsToString(): String {
        return authors?.joinToString(separator = ", ") ?: ""
    }

    fun isPending(): Boolean {
        return state == State.PENDING
    }

    fun isReading(): Boolean {
        return state == State.READING
    }
}