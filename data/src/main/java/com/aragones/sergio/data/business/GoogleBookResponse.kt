/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package com.aragones.sergio.data.business

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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