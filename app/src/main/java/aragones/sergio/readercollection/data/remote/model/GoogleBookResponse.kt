/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
data class GoogleBookListResponse(
    @Json(name = "totalItems")
    val totalItems: Int,
    @Json(name = "items")
    var items: List<GoogleBookResponse>?
)

@JsonClass(generateAdapter = true)
data class GoogleBookResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "volumeInfo")
    val volumeInfo: GoogleVolumeResponse
) {

    fun getGoogleBookIsbn(): String? {

        volumeInfo.industryIdentifiers?.mapNotNull { if (it.type == "ISBN_13") it.identifier else null }
            ?.let {
                if (it.isNotEmpty()) return it[0]
            }
        volumeInfo.industryIdentifiers?.mapNotNull { if (it.type == "ISBN_10") it.identifier else null }
            ?.let {
                if (it.isNotEmpty()) return it[0]
            }
        volumeInfo.industryIdentifiers?.mapNotNull { if (it.type == "OTHER") it.identifier else null }
            ?.let {
                if (it.isNotEmpty()) return it[0]
            }
        return null
    }

    fun getGoogleBookThumbnail(): String? {
        return volumeInfo.imageLinks?.thumbnail ?: volumeInfo.imageLinks?.smallThumbnail
    }

    fun getGoogleBookImage(): String? {
        return volumeInfo.imageLinks?.extraLarge ?: volumeInfo.imageLinks?.large
        ?: volumeInfo.imageLinks?.medium ?: volumeInfo.imageLinks?.small
    }
}

@JsonClass(generateAdapter = true)
data class GoogleVolumeResponse(
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
    @Json(name = "description")
    val description: String?,
    @Json(name = "industryIdentifiers")
    val industryIdentifiers: List<GoogleIsbnResponse>?,
    @Json(name = "pageCount")
    val pageCount: Int?,
    @Json(name = "categories")
    val categories: List<String>?,
    @Json(name = "averageRating")
    val averageRating: Double?,
    @Json(name = "ratingsCount")
    val ratingsCount: Int?,
    @Json(name = "imageLinks")
    val imageLinks: GoogleImageLinksResponse?
)

@JsonClass(generateAdapter = true)
data class GoogleIsbnResponse(
    @Json(name = "type")
    val type: String?,
    @Json(name = "identifier")
    val identifier: String?
)

@JsonClass(generateAdapter = true)
data class GoogleImageLinksResponse(
    @Json(name = "smallThumbnail")
    val smallThumbnail: String?,
    @Json(name = "thumbnail")
    val thumbnail: String?,
    @Json(name = "small")
    val small: String?,
    @Json(name = "medium")
    val medium: String?,
    @Json(name = "large")
    val large: String?,
    @Json(name = "extraLarge")
    val extraLarge: String?
)