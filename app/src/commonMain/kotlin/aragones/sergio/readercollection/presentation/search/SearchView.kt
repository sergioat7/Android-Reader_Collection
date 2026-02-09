/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SearchView(
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    ReaderCollectionApp {
        SearchScreen(
            state = state,
            onSearch = {
                viewModel.searchBooks(reload = true, query = it)
            },
            onFilter = viewModel::changeFilter,
            onBookClick = onBookClick,
            onSwipe = viewModel::addBook,
            onLoadMoreClick = viewModel::searchBooks,
            onRefresh = {
                viewModel.searchBooks(reload = true)
            },
            onBack = onBack,
        )
    }

    val text = if (infoDialogMessageId != null) {
        stringResource(requireNotNull(infoDialogMessageId))
    } else {
        ""
    }
    InformationAlertDialog(show = infoDialogMessageId != null, text = text) {
        viewModel.closeDialogs()
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
}