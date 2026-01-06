/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import aragones.sergio.readercollection.data.remote.DateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BookResponse(
    @SerialName("googleId")
    override val id: String = "",
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
    val pageCount: Int = 0,
    @SerialName("categories")
    val categories: List<String>? = null,
    @SerialName("averageRating")
    val averageRating: Double = 0.0,
    @SerialName("ratingsCount")
    val ratingsCount: Int = 0,
    @SerialName("rating")
    val rating: Double = 0.0,
    @SerialName("thumbnail")
    val thumbnail: String? = null,
    @SerialName("image")
    val image: String? = null,
    @SerialName("format")
    val format: String? = null,
    @SerialName("state")
    var state: String? = null,
    @SerialName("priority")
    var priority: Int = -1,
) : BaseModel<String>