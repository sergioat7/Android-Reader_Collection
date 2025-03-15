/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
 */

package aragones.sergio.readercollection.presentation.settings

data class SettingsUiState(
    val username: String,
    val password: String,
    val passwordError: Int?,
    val language: String,
    val sortParam: String?,
    val isSortDescending: Boolean,
    val themeMode: Int,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): SettingsUiState = SettingsUiState(
            username = "",
            password = "",
            passwordError = null,
            language = "",
            sortParam = null,
            isSortDescending = false,
            themeMode = 0,
            isLoading = false,
        )
    }
}