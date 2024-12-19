/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
 */

package aragones.sergio.readercollection.presentation.ui.register

import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState

data class RegisterUiState(
    val username: String,
    val password: String,
    val confirmPassword: String,
    val formState: LoginFormState,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): RegisterUiState {
            return RegisterUiState(
                username = "",
                password = "",
                confirmPassword = "",
                formState = LoginFormState(),
                isLoading = false,
            )
        }
    }
}