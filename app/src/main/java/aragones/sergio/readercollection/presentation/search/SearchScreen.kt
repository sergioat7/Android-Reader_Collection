/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/4/2024
 */

package aragones.sergio.readercollection.presentation.search

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.components.BookItem
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomSearchBar
import aragones.sergio.readercollection.presentation.components.ListButton
import aragones.sergio.readercollection.presentation.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.components.SwipeDirection
import aragones.sergio.readercollection.presentation.components.SwipeItem
import aragones.sergio.readercollection.presentation.components.SwipeItemBackground
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.roseBud
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
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

    val pullRefreshState = rememberPullToRefreshState()

    val elevation = if (showTopButton && !isLoading) 4.dp else 0.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        CustomSearchBar(
            title = stringResource(R.string.title_search),
            query = query ?: "",
            onSearch = onSearch,
            modifier = Modifier.shadow(elevation),
            backgroundColor = MaterialTheme.colorScheme.background,
            onBack = onBack,
        )

        val modifier = if (query != null) {
            Modifier.pullToRefresh(
                isRefreshing = isLoading,
                state = pullRefreshState,
                onRefresh = onRefresh,
            )
        } else {
            Modifier
        }

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
                            showTopButton = showTopButton && !isLoading,
                            showBottomButton = showBottomButton && !isLoading,
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
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = onRefresh,
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullRefreshState,
                contentAlignment = Alignment.TopCenter,
                indicator = {
                    Indicator(
                        state = pullRefreshState,
                        isRefreshing = isLoading,
                        modifier = Modifier.align(Alignment.TopCenter),
                        containerColor = MaterialTheme.colorScheme.primary,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                },
                content = {},
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
                        direction = SwipeDirection.LEFT,
                        dismissValue = SwipeToDismissBoxValue.EndToStart,
                        threshold = 0.6f,
                        onSwipe = { onSwipe(book.id) },
                        background = {
                            SwipeItemBackground(
                                dismissValue = SwipeToDismissBoxValue.EndToStart,
                                color = MaterialTheme.colorScheme.roseBud,
                                accessibilityPainter = painterResource(R.drawable.ic_save_book)
                                    .withDescription(stringResource(R.string.save)),
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
            painter = painterResource(R.drawable.ic_double_arrow_up)
                .withDescription(stringResource(R.string.go_to_start)),
            onClick = onTopButtonClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(topOffset.toInt(), 0) },
        )

        ListButton(
            painter = painterResource(R.drawable.ic_double_arrow_down)
                .withDescription(stringResource(R.string.go_to_end)),
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
        modifier = modifier.padding(12.dp).widthIn(max = 320.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.tertiary,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(R.drawable.ic_add_circle_outline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(R.string.load_more),
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
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

@CustomPreviewLightDark
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
                        id = "1",
                        title = "Book 1",
                        subtitle = null,
                        authors = listOf("Author"),
                        publisher = null,
                        publishedDate = null,
                        readingDate = null,
                        description = null,
                        summary = null,
                        isbn = null,
                        pageCount = 0,
                        categories = null,
                        averageRating = 0.0,
                        ratingsCount = 0,
                        rating = 6.0,
                        thumbnail = null,
                        image = null,
                        format = null,
                        state = null,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Book 2",
                        subtitle = null,
                        authors = listOf("Author"),
                        publisher = null,
                        publishedDate = null,
                        readingDate = null,
                        description = null,
                        summary = null,
                        isbn = null,
                        pageCount = 0,
                        categories = null,
                        averageRating = 0.0,
                        ratingsCount = 0,
                        rating = 5.0,
                        thumbnail = null,
                        image = null,
                        format = null,
                        state = null,
                        priority = 0,
                    ),
                    Book(
                        id = "3",
                        title = "Book 3",
                        subtitle = null,
                        authors = null,
                        publisher = null,
                        publishedDate = null,
                        readingDate = null,
                        description = null,
                        summary = null,
                        isbn = null,
                        pageCount = 0,
                        categories = null,
                        averageRating = 0.0,
                        ratingsCount = 0,
                        rating = 0.0,
                        thumbnail = null,
                        image = null,
                        format = null,
                        state = null,
                        priority = 0,
                    ),
                    Book(
                        id = "",
                        title = "",
                        subtitle = null,
                        authors = null,
                        publisher = null,
                        publishedDate = null,
                        readingDate = null,
                        description = null,
                        summary = null,
                        isbn = null,
                        pageCount = 0,
                        categories = null,
                        averageRating = 0.0,
                        ratingsCount = 0,
                        rating = 0.0,
                        thumbnail = null,
                        image = null,
                        format = null,
                        state = null,
                        priority = 0,
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