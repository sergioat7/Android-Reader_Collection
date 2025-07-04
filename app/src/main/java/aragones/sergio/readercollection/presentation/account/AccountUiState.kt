/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.account

data class AccountUiState(
    val username: String,
    val password: String,
    val passwordError: Int?,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): AccountUiState = AccountUiState(
            username = "",
            password = "",
            passwordError = null,
            isLoading = false,
        )
    }
}
