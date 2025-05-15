/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/12/2024
 */

package aragones.sergio.readercollection.presentation.books

import aragones.sergio.readercollection.domain.model.Book

sealed class BooksUiState {
    abstract val query: String
    abstract val isLoading: Boolean

    data class Empty(
        override val query: String,
        override val isLoading: Boolean,
    ) : BooksUiState()

    data class Success(
        val books: List<Book>,
        override val query: String,
        override val isLoading: Boolean,
    ) : BooksUiState()
}
