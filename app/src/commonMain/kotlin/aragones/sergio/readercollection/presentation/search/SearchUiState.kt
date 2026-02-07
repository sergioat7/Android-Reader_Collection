/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 21/8/2024
 */

package aragones.sergio.readercollection.presentation.search

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.ui.graphics.vector.ImageVector
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import org.jetbrains.compose.resources.StringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.authors
import reader_collection.app.generated.resources.title

sealed class SearchUiState {

    abstract val param: SearchParam

    data object Empty : SearchUiState() {
        override val param: SearchParam
            get() = SearchParam.TITLE
    }

    data class Success(
        val isLoading: Boolean,
        val query: String?,
        val books: Books,
        override val param: SearchParam,
    ) : SearchUiState()

    data class Error(
        val isLoading: Boolean,
        val query: String?,
        val value: ErrorModel,
        override val param: SearchParam,
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