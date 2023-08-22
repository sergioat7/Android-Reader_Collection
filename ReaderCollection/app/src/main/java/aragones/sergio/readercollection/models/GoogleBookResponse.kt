/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.models

import com.google.gson.annotations.SerializedName

data class GoogleBookResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("volumeInfo")
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