/*
 * Copyright (c) 2023 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2023
 */

package aragones.sergio.readercollection.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleBookListResponse(
    @Json(name = "totalItems")
    val totalItems: Int,
    @Json(name = "items")
    var items: List<GoogleBookResponse>?
)