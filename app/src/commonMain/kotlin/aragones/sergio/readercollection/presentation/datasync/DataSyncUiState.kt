/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

data class DataSyncUiState(
    val isAutomaticSyncEnabled: Boolean,
    val isLoading: Boolean,
) {
    companion object {
        fun empty(): DataSyncUiState = DataSyncUiState(
            isAutomaticSyncEnabled = true,
            isLoading = false,
        )
    }
}