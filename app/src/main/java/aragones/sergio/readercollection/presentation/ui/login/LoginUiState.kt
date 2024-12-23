/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
 */

package aragones.sergio.readercollection.presentation.ui.login

import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState

data class LoginUiState(
    val username: String,
    val password: String,
    val formState: LoginFormState,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): LoginUiState = LoginUiState(
            username = "",
            password = "",
            formState = LoginFormState(),
            isLoading = false,
        )
    }
}