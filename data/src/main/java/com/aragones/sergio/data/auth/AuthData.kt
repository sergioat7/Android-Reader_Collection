/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package com.aragones.sergio.data.auth

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthData(
    var token: String
)