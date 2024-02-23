/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package com.aragones.sergio.data.business

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date

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