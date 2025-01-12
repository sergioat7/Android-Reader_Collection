/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.ui.components.InformationAlertDialog

@Composable
fun SearchView(
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state
    val infoDialogMessageId by viewModel.infoDialogMessageId.observeAsState(initial = -1)

    SearchScreen(
        state = state,
        onBookClick = onBookClick,
        onSwipe = viewModel::addBook,
        onSearch = {
            viewModel.searchBooks(reload = true, query = it)
        },
        onLoadMoreClick = viewModel::searchBooks,
        onRefresh = {
            viewModel.searchBooks(reload = true)
        },
        onBack = onBack,
    )

    val text = if (infoDialogMessageId != -1) {
        stringResource(infoDialogMessageId)
    } else {
        ""
    }
    InformationAlertDialog(show = infoDialogMessageId != -1, text = text) {
        viewModel.closeDialogs()
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
}