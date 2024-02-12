/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

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