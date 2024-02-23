/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package com.aragones.sergio.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GoogleIsbnResponse(
    @Json(name = "type")
    val type: String?,
    @Json(name = "identifier")
    val identifier: String?
)