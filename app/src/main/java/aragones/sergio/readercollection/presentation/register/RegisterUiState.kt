/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
 */

package aragones.sergio.readercollection.presentation.register

import aragones.sergio.readercollection.presentation.login.model.LoginFormState

data class RegisterUiState(
    val username: String,
    val password: String,
    val confirmPassword: String,
    val formState: LoginFormState,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): RegisterUiState = RegisterUiState(
            username = "",
            password = "",
            confirmPassword = "",
            formState = LoginFormState(),
            isLoading = false,
        )
    }
}