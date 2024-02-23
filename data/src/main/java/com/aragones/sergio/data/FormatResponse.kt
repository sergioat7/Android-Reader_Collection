/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/1/2022
 */

package com.aragones.sergio.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FormatResponse(
    override val id: String,
    val name: String
) : BaseModel<String>