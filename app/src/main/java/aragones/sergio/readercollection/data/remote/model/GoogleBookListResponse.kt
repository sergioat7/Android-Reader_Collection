/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package aragones.sergio.readercollection.data.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleBookListResponse(
    @Json(name = "totalItems")
    val totalItems: Int,
    @Json(name = "items")
    var items: List<GoogleBookResponse>?
)