/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.models

import com.google.gson.annotations.SerializedName

data class LoginFormState(
    @SerializedName("usernameError")
    val usernameError: Int? = null,
    @SerializedName("passwordError")
    val passwordError: Int? = null,
    @SerializedName("isDataValid")
    val isDataValid: Boolean = false
)