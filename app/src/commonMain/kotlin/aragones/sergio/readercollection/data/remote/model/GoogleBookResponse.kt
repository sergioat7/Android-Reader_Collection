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
data class GoogleBookListResponse(
    @SerialName("totalItems")
    val totalItems: Int,
    @SerialName("items")
    var items: List<GoogleBookResponse>? = null,
)

@Serializable
data class GoogleBookResponse(
    @SerialName("id")
    val id: String,
    @SerialName("volumeInfo")
    val volumeInfo: GoogleVolumeResponse,
) {

    fun getGoogleBookIsbn(): String? {
        volumeInfo.industryIdentifiers
            ?.mapNotNull { if (it.type == "ISBN_13") it.identifier else null }
            ?.let { if (it.isNotEmpty()) return it[0] }
        volumeInfo.industryIdentifiers
            ?.mapNotNull { if (it.type == "ISBN_10") it.identifier else null }
            ?.let { if (it.isNotEmpty()) return it[0] }
        volumeInfo.industryIdentifiers
            ?.mapNotNull { if (it.type == "OTHER") it.identifier else null }
            ?.let { if (it.isNotEmpty()) return it[0] }
        return null
    }

    fun getCategories(): List<String>? = volumeInfo.categories
        ?.joinToString(" / ")
        ?.split("/")
        ?.map { it.trim() }
        ?.distinct()

    fun getGoogleBookThumbnail(): String? =
        volumeInfo.imageLinks?.thumbnail ?: volumeInfo.imageLinks?.smallThumbnail

    fun getGoogleBookImage(): String? =
        volumeInfo.imageLinks?.extraLarge ?: volumeInfo.imageLinks?.large
            ?: volumeInfo.imageLinks?.medium ?: volumeInfo.imageLinks?.small
}

@Serializable
data class GoogleVolumeResponse(
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
    @SerialName("description")
    val description: String? = null,
    @SerialName("industryIdentifiers")
    val industryIdentifiers: List<GoogleIsbnResponse>? = null,
    @SerialName("pageCount")
    val pageCount: Int? = null,
    @SerialName("categories")
    val categories: List<String>? = null,
    @SerialName("averageRating")
    val averageRating: Double? = null,
    @SerialName("ratingsCount")
    val ratingsCount: Int? = null,
    @SerialName("imageLinks")
    val imageLinks: GoogleImageLinksResponse? = null,
)

@Serializable
data class GoogleIsbnResponse(
    @SerialName("type")
    val type: String? = null,
    @SerialName("identifier")
    val identifier: String? = null,
)

@Serializable
data class GoogleImageLinksResponse(
    @SerialName("smallThumbnail")
    val smallThumbnail: String? = null,
    @SerialName("thumbnail")
    val thumbnail: String? = null,
    @SerialName("small")
    val small: String? = null,
    @SerialName("medium")
    val medium: String? = null,
    @SerialName("large")
    val large: String? = null,
    @SerialName("extraLarge")
    val extraLarge: String? = null,
)