/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.login.model

import org.jetbrains.compose.resources.StringResource

data class LoginFormState(
    val usernameError: StringResource? = null,
    val passwordError: StringResource? = null,
    val isDataValid: Boolean = false,
)