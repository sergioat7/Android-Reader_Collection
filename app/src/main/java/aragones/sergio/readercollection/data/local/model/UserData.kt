/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.data.local.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserData(
    var username: String,
    var password: String,
    var isLoggedIn: Boolean
)