/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/9/2024
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import aragones.sergio.readercollection.domain.model.Book

sealed class BookListUiState {

    data object Empty : BookListUiState()

    data class Success(
        val isLoading: Boolean,
        val books: List<Book>,
        val isDraggingEnabled: Boolean,
    ) : BookListUiState() {
        companion object {
            fun initial(): Success = Success(
                isLoading = true,
                books = listOf(),
                isDraggingEnabled = false,
            )
        }
    }
}