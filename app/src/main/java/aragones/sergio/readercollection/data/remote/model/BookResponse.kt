/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class BookResponse(
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
    var priority: Int,
) : BaseModel<String>