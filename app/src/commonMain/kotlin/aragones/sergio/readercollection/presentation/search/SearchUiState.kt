/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2024
 */

package aragones.sergio.readercollection.presentation.search

import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel

sealed class SearchUiState {

    data object Empty : SearchUiState()

    data class Success(
        val isLoading: Boolean,
        val query: String?,
        val books: Books,
    ) : SearchUiState()

    data class Error(
        val isLoading: Boolean,
        val query: String?,
        val value: ErrorModel,
    ) : SearchUiState()
}

enum class SearchParam(
    val value: StringResource,
    val icon: ImageVector,
    val key: String,
) {
    TITLE(Res.string.title, Icons.Default.Title, "intitle"),
    AUTHOR(Res.string.authors, Icons.Default.Person, "inauthor"),
}