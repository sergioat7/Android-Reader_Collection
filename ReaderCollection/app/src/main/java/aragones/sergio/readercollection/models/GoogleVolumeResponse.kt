/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models

import aragones.sergio.readercollection.models.GoogleImageLinksResponse
import aragones.sergio.readercollection.models.GoogleIsbnResponse
import com.google.gson.annotations.SerializedName
import java.util.*

data class GoogleVolumeResponse(
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
    @SerializedName("description")
    val description: String?,
    @SerializedName("industryIdentifiers")
    val industryIdentifiers: List<GoogleIsbnResponse>?,
    @SerializedName("pageCount")
    val pageCount: Int?,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("averageRating")
    val averageRating: Double?,
    @SerializedName("ratingsCount")
    val ratingsCount: Int?,
    @SerializedName("imageLinks")
    val imageLinks: GoogleImageLinksResponse?
)