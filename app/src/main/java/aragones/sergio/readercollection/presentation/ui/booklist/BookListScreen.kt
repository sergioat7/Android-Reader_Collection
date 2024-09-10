/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/9/2024
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.BookItem
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.ListButton
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.search.reachedBottom
import com.aragones.sergio.util.BookState
import kotlinx.coroutines.launch

@Composable
fun BookListScreen(
    state: BookListUiState,
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    onDragClick: () -> Unit,
    onSortClick: () -> Unit,
) {

    val listState = rememberLazyListState()
    val showTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex != 0
        }
    }
    val showBottomButton by remember {
        derivedStateOf {
            !listState.reachedBottom()
        }
    }
    val coroutineScope = rememberCoroutineScope()

    val title = if (state is BookListUiState.Success && state.books.isNotEmpty()) {
        pluralStringResource(
            R.plurals.title_books_count,
            state.books.size,
            state.books.size
        )
    } else ""
    val (showActions, isDraggingEnabled) = when (state) {
        is BookListUiState.Success -> {
            (state.books.firstOrNull()?.isPending() == false) to state.isDraggingEnabled
        }

        else -> false to false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CustomToolbar(
            title = title,
            modifier = Modifier.background(MaterialTheme.colors.background),
            elevation = if (showTopButton) 4.dp else 0.dp,
            onBack = onBack,
            actions = {
                if (showActions) {
                    TopAppBarIcon(
                        icon = if (isDraggingEnabled) R.drawable.ic_enable_drag else R.drawable.ic_disable_drag,
                        onClick = onDragClick,
                    )
                    TopAppBarIcon(
                        icon = R.drawable.ic_sort_books,
                        onClick = onSortClick,
                    )
                }
            }
        )
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            when (state) {
                is BookListUiState.Empty -> onBack()
                is BookListUiState.Success -> {
                    if (state.books.isEmpty()) {
                        NoResultsComponent()
                    } else {
                        BookListContent(
                            books = state.books,
                            listState = listState,
                            showTopButton = showTopButton,
                            showBottomButton = showBottomButton,
                            onTopButtonClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index = 0)
                                }
                            },
                            onBottomButtonClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index = listState.layoutInfo.totalItemsCount - 1)
                                }
                            },
                            onBookClick = onBookClick,
                        )
                    }
                }

                is BookListUiState.Error -> NoResultsComponent(text = stringResource(id = state.value.errorKey))
            }

            if (state is BookListUiState.Success && state.isLoading) {
                CustomCircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BookListContent(
    books: List<Book>,
    listState: LazyListState,
    showTopButton: Boolean,
    showBottomButton: Boolean,
    onTopButtonClick: () -> Unit,
    onBottomButtonClick: () -> Unit,
    onBookClick: (String) -> Unit,
) {

    val topOffset by animateFloatAsState(
        targetValue = if (showTopButton) 0f else 100f,
        label = ""
    )
    val bottomOffset by animateFloatAsState(
        targetValue = if (showBottomButton) 0f else 100f,
        label = ""
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(items = books) { book ->
                BookItem(book = book, onBookClick = onBookClick)
            }
        }
        ListButton(
            image = R.drawable.ic_double_arrow_up,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = topOffset.dp),
            onClick = onTopButtonClick,
        )
        ListButton(
            image = R.drawable.ic_double_arrow_down,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = bottomOffset.dp),
            onClick = onBottomButtonClick,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PendingBookListScreenSuccessPreview() {
    BookListScreen(
        state = BookListUiState.Success(
            isLoading = true,
            books = listOf(
                Book(
                    "1",
                    "Large title for stored book in the list that should not be shown",
                    null,
                    listOf("Author"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null,
                    0.0,
                    0,
                    5.0,
                    null,
                    null,
                    null,
                    BookState.PENDING,
                    false,
                    0
                ),
                Book(
                    "2",
                    "Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null,
                    0.0,
                    0,
                    0.0,
                    null,
                    null,
                    null,
                    BookState.PENDING,
                    false,
                    0
                )
            ),
            isDraggingEnabled = false,
        ),
        onBookClick = {},
        onBack = {},
        onDragClick = {},
        onSortClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun ReadBookListScreenSuccessPreview() {
    BookListScreen(
        state = BookListUiState.Success(
            isLoading = true,
            books = listOf(
                Book(
                    "1",
                    "Large title for stored book in the list that should not be shown",
                    null,
                    listOf("Author"),
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null,
                    0.0,
                    0,
                    5.0,
                    null,
                    null,
                    null,
                    BookState.READ,
                    false,
                    0
                ),
                Book(
                    "2",
                    "Title",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    0,
                    null,
                    0.0,
                    0,
                    0.0,
                    null,
                    null,
                    null,
                    BookState.READ,
                    false,
                    0
                )
            ),
            isDraggingEnabled = true,
        ),
        onBookClick = {},
        onBack = {},
        onDragClick = {},
        onSortClick = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun BookListScreenErrorPreview() {
    BookListScreen(
        state = BookListUiState.Error(
            value = ErrorResponse("", R.string.error_database)
        ),
        onBookClick = {},
        onBack = {},
        onDragClick = {},
        onSortClick = {},
    )
}