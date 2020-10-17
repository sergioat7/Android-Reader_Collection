/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.models

import aragones.sergio.readercollection.models.base.BaseModel
import com.google.gson.annotations.SerializedName
import java.util.*

data class BookResponse(
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
    val isbn:  String?,
    @SerializedName("pageCount")
    val pageCount: Int,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("averageRating")
    val averageRating: Int,
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
    val state: String?,
    @SerializedName("isFavourite")
    val isFavourite: Boolean
): BaseModel<String>