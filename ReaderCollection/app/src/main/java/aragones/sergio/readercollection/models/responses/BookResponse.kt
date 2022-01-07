/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models.responses

import androidx.room.Entity
import androidx.room.PrimaryKey
import aragones.sergio.readercollection.base.BaseModel
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
    var isFavourite: Boolean
): BaseModel<String> {
    constructor(id: String): this(
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
        false)
}