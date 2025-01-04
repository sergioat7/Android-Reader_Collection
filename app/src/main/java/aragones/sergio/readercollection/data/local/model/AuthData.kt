/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.data.local.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthData(
    var token: String,
)