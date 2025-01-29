/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.ui.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.ui.components.SortingPickerAlertDialog

@Composable
fun BookListView(
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BookListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.state
    val sortingPickerState by viewModel.sortingPickerState
    val error by viewModel.booksError.observeAsState()

    when (val currentState = uiState) {
        is BookListUiState.Success -> {
            if (currentState.books.isEmpty() && !currentState.isLoading) {
                onBack()
                return
            }
        }
        else -> {
            Unit
        }
    }

    BookListScreen(
        state = uiState,
        onBookClick = onBookClick,
        onBack = onBack,
        onDragClick = {
            viewModel.switchDraggingState()
        },
        onSortClick = {
            viewModel.showSortingPickerState()
        },
        onDrag = {
            viewModel.updateBookOrdering(it)
        },
        onDragEnd = {
            viewModel.setPriorityFor(it)
        },
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
        onBack()
    }

    LaunchedEffectOnce {
        viewModel.fetchBooks()
    }
}