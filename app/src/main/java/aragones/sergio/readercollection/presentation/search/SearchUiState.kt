/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2024
 */

package aragones.sergio.readercollection.presentation.search

import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.model.Book

sealed class SearchUiState {

    data object Empty : SearchUiState()

    data class Success(
        val isLoading: Boolean,
        val query: String?,
        val books: List<Book>,
    ) : SearchUiState()

    data class Error(
        val isLoading: Boolean,
        val query: String?,
        val value: ErrorResponse,
    ) : SearchUiState()
}