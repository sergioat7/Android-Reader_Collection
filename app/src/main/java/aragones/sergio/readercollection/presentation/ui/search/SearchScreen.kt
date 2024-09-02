/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/4/2024
 */

@file:OptIn(ExperimentalMaterialApi::class)

package aragones.sergio.readercollection.presentation.ui.search

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.BookItem
import aragones.sergio.readercollection.presentation.ui.components.CustomSearchBar
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.SwipeItem
import aragones.sergio.readercollection.presentation.ui.components.SwipeItemBackground
import aragones.sergio.readercollection.presentation.ui.theme.roseBud
import kotlinx.coroutines.launch

@Preview(showBackground = true)
@Composable
fun SearchScreenPreviewSuccess() {
    SearchScreen(
        state = SearchUiState.Success(
            books = listOf(
                Book(
                    "1",
                    "Harry Potter y la Piedra Filosofal",
                    null,
                    listOf("J.K. Rowling"),
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
                    0
                ),
                Book(
                    "2",
                    "Large title for another searched book in the list that should not be shown",
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
                    0
                ),
                Book(
                    "3",
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
                    null,
                    false,
                    0
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
                    0
                )
            ),
            isLoading = true,
            query = null,
        ),
        onSearch = {},
        onBookClick = {},
        onSwipe = {},
        onLoadMoreClick = {},
        onRefresh = {},
    )
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreviewError() {
    SearchScreen(
        state = SearchUiState.Error(
            isLoading = false,
            query = null,
            value = ErrorResponse("", 0)
        ),
        onSearch = {},
        onBookClick = {},
        onSwipe = {},
        onLoadMoreClick = {},
        onRefresh = {},
    )
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onSwipe: (String) -> Unit,
    onLoadMoreClick: () -> Unit,
    onRefresh: () -> Unit,
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
            modifier = Modifier.background(MaterialTheme.colors.background),
            elevation = if (showTopButton) 4.dp else 0.dp,
            onSearch = {
                onSearch(it)
            },
        )

        val modifier = if (query != null) Modifier.pullRefresh(pullRefreshState) else Modifier

        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            when (state) {
                is SearchUiState.Empty -> NoResultsComponent(text = stringResource(id = R.string.no_search_yet_text))
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
                                    listState.animateScrollToItem(index = listState.layoutInfo.totalItemsCount - 1)
                                }
                            },
                            onBookClick = onBookClick,
                            onSwipe = onSwipe,
                            onLoadMoreClick = onLoadMoreClick,
                        )
                    }
                }

                is SearchUiState.Error -> ErrorContent()
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
    LazyColumn {
        item {
            NoResultsComponent()
        }
    }
}

@Composable
private fun ErrorContent() {
    LazyColumn {
        item {
            NoResultsComponent(text = stringResource(id = R.string.error_server))
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
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(items = books, key = { it.id }) { book ->
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
                            BookItem(book = book, onBookClick = onBookClick)
                        },
                    )
                } else {
                    LoadMoreButton(onLoadMoreClick)
                }
            }
        }

        val topOffset by animateFloatAsState(
            targetValue = if (showTopButton) 0f else 100f,
            label = ""
        )
        val bottomOffset by animateFloatAsState(
            targetValue = if (showBottomButton) 0f else 100f,
            label = ""
        )

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

@Composable
private fun ListButton(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        contentColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Icon(
            painter = painterResource(id = image),
            contentDescription = "",
            tint = MaterialTheme.colors.secondary,
        )
    }
}

@Composable
private fun LoadMoreButton(onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.primary,
                disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
            ),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_circle_outline),
                    contentDescription = "",
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
}

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}
