/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package com.aragones.sergio.data

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