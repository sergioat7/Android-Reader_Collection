/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/9/2024
 */

package aragones.sergio.readercollection.presentation.booklist

import aragones.sergio.readercollection.domain.model.Books

data class BookListUiState(
    val isLoading: Boolean,
    val books: Books,
    val subtitle: String,
    val isDraggingEnabled: Boolean,
) {
    companion object {
        fun initial(): BookListUiState = BookListUiState(
            isLoading = true,
            books = Books(),
            subtitle = "",
            isDraggingEnabled = false,
        )
    }
}