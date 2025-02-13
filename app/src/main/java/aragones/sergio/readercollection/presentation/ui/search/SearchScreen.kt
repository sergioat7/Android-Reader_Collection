/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/4/2024
 */

@file:OptIn(ExperimentalMaterialApi::class)

package aragones.sergio.readercollection.presentation.ui.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.BookItem
import aragones.sergio.readercollection.presentation.ui.components.CustomSearchBar
import aragones.sergio.readercollection.presentation.ui.components.ListButton
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.SwipeItem
import aragones.sergio.readercollection.presentation.ui.components.SwipeItemBackground
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.roseBud
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    state: SearchUiState,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onSwipe: (String) -> Unit,
    onLoadMoreClick: () -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit,
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

    val (isLoading, query) = when (state) {
        SearchUiState.Empty -> false to null
        is SearchUiState.Success -> state.isLoading to state.query
        is SearchUiState.Error -> state.isLoading to state.query
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = onRefresh,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CustomSearchBar(
            title = stringResource(id = R.string.title_search),
            query = query ?: "",
            onSearch = onSearch,
            modifier = Modifier.background(MaterialTheme.colors.background),
            elevation = if (showTopButton) 4.dp else 0.dp,
            onBack = onBack,
        )

        val modifier = if (query != null) Modifier.pullRefresh(pullRefreshState) else Modifier

        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            when (state) {
                is SearchUiState.Empty -> {
                    NoResultsComponent(
                        text = stringResource(R.string.no_search_yet_text),
                        image = R.drawable.image_no_search,
                    )
                }
                is SearchUiState.Success -> {
                    if (state.books.isEmpty() && !state.isLoading) {
                        NoResultsContent()
                    } else {
                        SearchContent(
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
                                    listState.animateScrollToItem(
                                        index = listState.layoutInfo.totalItemsCount - 1,
                                    )
                                }
                            },
                            onBookClick = onBookClick,
                            onSwipe = onSwipe,
                            onLoadMoreClick = onLoadMoreClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                is SearchUiState.Error -> {
                    ErrorContent()
                }
            }
            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                backgroundColor = MaterialTheme.colors.secondary,
                contentColor = MaterialTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun NoResultsContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            NoResultsComponent()
        }
    }
}

@Composable
private fun ErrorContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
    ) {
        item {
            NoResultsComponent(text = stringResource(R.string.error_server))
        }
    }
}

@Composable
private fun SearchContent(
    books: List<Book>,
    listState: LazyListState,
    showTopButton: Boolean,
    showBottomButton: Boolean,
    onTopButtonClick: () -> Unit,
    onBottomButtonClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onSwipe: (String) -> Unit,
    onLoadMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        LazyColumn(
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            itemsIndexed(books) { index, book ->
                if (book.id.isNotBlank()) {
                    SwipeItem(
                        direction = DismissDirection.EndToStart,
                        dismissValue = DismissValue.DismissedToStart,
                        threshold = 0.6f,
                        onSwipe = { onSwipe(book.id) },
                        background = {
                            SwipeItemBackground(
                                dismissValue = DismissValue.DismissedToStart,
                                color = MaterialTheme.colors.roseBud,
                                icon = R.drawable.ic_save_book,
                            )
                        },
                        content = {
                            BookItem(
                                book = book,
                                onBookClick = onBookClick,
                                showDivider = index < books.size - 1,
                            )
                        },
                    )
                } else {
                    LoadMoreButton(onLoadMoreClick)
                }
            }
        }

        val topOffset by animateFloatAsState(
            targetValue = if (showTopButton) 0f else 200f,
            label = "",
        )
        val bottomOffset by animateFloatAsState(
            targetValue = if (showBottomButton) 0f else 200f,
            label = "",
        )

        ListButton(
            image = R.drawable.ic_double_arrow_up,
            onClick = onTopButtonClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(topOffset.toInt(), 0) },
        )

        ListButton(
            image = R.drawable.ic_double_arrow_down,
            onClick = onBottomButtonClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset { IntOffset(bottomOffset.toInt(), 0) },
        )
    }
}

@Composable
private fun LoadMoreButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add_circle_outline),
                contentDescription = null,
                tint = MaterialTheme.colors.secondary,
            )
            Text(
                text = stringResource(id = R.string.load_more),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.button,
                color = MaterialTheme.colors.secondary,
                maxLines = 1,
            )
        }
    }
}

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 &&
        lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}

@PreviewLightDark
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(SearchScreenPreviewParameterProvider::class) state: SearchUiState,
) {
    ReaderCollectionTheme {
        SearchScreen(
            state = state,
            onSearch = {},
            onBookClick = {},
            onSwipe = {},
            onLoadMoreClick = {},
            onRefresh = {},
            onBack = {},
        )
    }
}

private class SearchScreenPreviewParameterProvider :
    PreviewParameterProvider<SearchUiState> {

    override val values: Sequence<SearchUiState>
        get() = sequenceOf(
            SearchUiState.Success(
                books = listOf(
                    Book(
                        "1",
                        "Book 1",
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
                        6.0,
                        null,
                        null,
                        null,
                        null,
                        false,
                        0,
                    ),
                    Book(
                        "2",
                        "Book 2",
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
                        null,
                        false,
                        0,
                    ),
                    Book(
                        "3",
                        "Book 3",
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
                        null,
                        false,
                        0,
                    ),
                    Book(
                        "",
                        "",
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
                        null,
                        false,
                        0,
                    ),
                ),
                isLoading = true,
                query = null,
            ),
            SearchUiState.Success(
                books = emptyList(),
                isLoading = false,
                query = null,
            ),
            SearchUiState.Empty,
            SearchUiState.Error(
                isLoading = false,
                query = null,
                value = ErrorResponse("", 0),
            ),
        )
}