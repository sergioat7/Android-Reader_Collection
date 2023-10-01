/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models

import com.google.gson.annotations.SerializedName

data class LoginCredentials(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)