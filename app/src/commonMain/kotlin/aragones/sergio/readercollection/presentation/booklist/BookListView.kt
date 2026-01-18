/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.booklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.components.SortingPickerAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookListView(
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: BookListViewModel = koinViewModel(),
) {
    val uiState by viewModel.state.collectAsState()
    val sortingPickerState by viewModel.sortingPickerState.collectAsState()
    val error by viewModel.booksError.collectAsState()

    if (uiState.books.books.isEmpty() && !uiState.isLoading) {
        onBack()
        return
    }

    ReaderCollectionApp {
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
    }

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