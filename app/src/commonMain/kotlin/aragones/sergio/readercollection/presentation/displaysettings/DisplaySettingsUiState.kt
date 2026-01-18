/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.displaysettings

data class DisplaySettingsUiState(
    val language: String,
    val sortParam: String?,
    val isSortDescending: Boolean,
    val themeMode: Int,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): DisplaySettingsUiState = DisplaySettingsUiState(
            language = "",
            sortParam = null,
            isSortDescending = false,
            themeMode = 0,
            isLoading = false,
        )
    }
}