/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.data.local.model

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    var username: String,
    var password: String,
)