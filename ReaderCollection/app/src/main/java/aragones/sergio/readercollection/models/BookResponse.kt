/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import aragones.sergio.readercollection.models.base.BaseModel
import aragones.sergio.readercollection.extensions.toString
import aragones.sergio.readercollection.data.source.SharedPreferencesHandler
import aragones.sergio.readercollection.utils.State
import com.google.gson.annotations.SerializedName
import java.util.*

@Entity(tableName = "Book")
data class BookResponse(
    @PrimaryKey
    @SerializedName("googleId")
    override val id: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("subtitle")
    val subtitle: String?,
    @SerializedName("authors")
    val authors: List<String>?,
    @SerializedName("publisher")
    val publisher: String?,
    @SerializedName("publishedDate")
    val publishedDate: Date?,
    @SerializedName("readingDate")
    val readingDate: Date?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("summary")
    val summary: String?,
    @SerializedName("isbn")
    val isbn: String?,
    @SerializedName("pageCount")
    val pageCount: Int,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("averageRating")
    val averageRating: Double,
    @SerializedName("ratingsCount")
    val ratingsCount: Int,
    @SerializedName("rating")
    val rating: Double,
    @SerializedName("thumbnail")
    val thumbnail: String?,
    @SerializedName("image")
    val image: String?,
    @SerializedName("format")
    val format: String?,
    @SerializedName("state")
    var state: String?,
    @SerializedName("isFavourite")
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

    fun publishedDateAsHumanReadable(): String? {

        return publishedDate.toString(
            SharedPreferencesHandler.dateFormatToShow,
            SharedPreferencesHandler.language
        )
    }

    fun readingDateAsHumanReadable(): String? {

        return readingDate.toString(
            SharedPreferencesHandler.dateFormatToShow,
            SharedPreferencesHandler.language
        )
    }

    fun isPending(): Boolean {
        return state == State.PENDING
    }

    fun isReading(): Boolean {
        return state == State.READING
    }
}