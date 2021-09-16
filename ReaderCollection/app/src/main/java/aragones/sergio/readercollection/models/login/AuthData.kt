/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/10/2020
 */

package aragones.sergio.readercollection.models.login

import com.google.gson.annotations.SerializedName

data class AuthData(
    @SerializedName("token")
    var token: String
)