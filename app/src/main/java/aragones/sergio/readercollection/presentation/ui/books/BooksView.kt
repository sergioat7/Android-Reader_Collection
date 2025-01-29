/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.books

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.ui.components.SortingPickerAlertDialog

@Composable
fun BooksView(
    onBookClick: (String) -> Unit,
    onShowAll: (String, String?, Boolean, String) -> Unit,
    onAddBook: () -> Unit,
    viewModel: BooksViewModel = hiltViewModel(),
) {
    val state by viewModel.state
    val sortingPickerState by viewModel.sortingPickerState
    val error by viewModel.booksError.collectAsState()

    BooksScreen(
        state = state,
        onSortClick = {
            viewModel.showSortingPickerState()
        },
        onSearch = {
            viewModel.searchBooks(it)
        },
        onBookClick = onBookClick,
        onShowAll = { bookState ->
            onShowAll(
                bookState,
                sortingPickerState.sortParam,
                sortingPickerState.isSortDescending,
                state.query,
            )
        },
        onSwitchToLeft = { fromIndex ->
            viewModel.switchBooksPriority(fromIndex, fromIndex - 1)
        },
        onSwitchToRight = { fromIndex ->
            viewModel.switchBooksPriority(fromIndex, fromIndex + 1)
        },
        onBookStateChange = viewModel::setBook,
        onAddBook = onAddBook,
    )

    SortingPickerAlertDialog(
        state = sortingPickerState,
        onCancel = {
            viewModel.updatePickerState(
                sortingPickerState.sortParam,
                sortingPickerState.isSortDescending,
            )
        },
        onAccept = { newSortParam, newIsSortDescending ->
            viewModel.updatePickerState(newSortParam, newIsSortDescending)
        },
    )

    val text = if (error != null) {
        val errorText = StringBuilder()
        if (requireNotNull(error).error.isNotEmpty()) {
            errorText.append(requireNotNull(error).error)
        } else {
            errorText.append(stringResource(requireNotNull(error).errorKey))
        }
        errorText.toString()
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }

    LaunchedEffectOnce {
        viewModel.fetchBooks()
    }
}