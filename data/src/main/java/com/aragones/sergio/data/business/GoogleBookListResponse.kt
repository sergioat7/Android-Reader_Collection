/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package com.aragones.sergio.data.business

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleBookListResponse(
    @Json(name = "totalItems")
    val totalItems: Int,
    @Json(name = "items")
    var items: List<GoogleBookResponse>?
)