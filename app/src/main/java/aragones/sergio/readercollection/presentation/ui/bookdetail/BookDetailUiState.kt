/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/2/2025
 */

package aragones.sergio.readercollection.presentation.ui.bookdetail

import aragones.sergio.readercollection.domain.model.Book

data class BookDetailUiState(
    val book: Book?,
    val isAlreadySaved: Boolean,
    val isEditable: Boolean,
)