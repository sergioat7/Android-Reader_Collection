/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.login.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginFormState(
    @Json(name = "usernameError")
    val usernameError: Int? = null,
    @Json(name = "passwordError")
    val passwordError: Int? = null,
    @Json(name = "isDataValid")
    val isDataValid: Boolean = false,
)