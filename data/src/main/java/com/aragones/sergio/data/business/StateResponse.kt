/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 23/2/2024
 */

package com.aragones.sergio.data.business

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StateResponse(
    override val id: String,
    val name: String
) : BaseModel<String>