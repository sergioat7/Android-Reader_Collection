/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/9/2024
 */

package aragones.sergio.readercollection.presentation.booklist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.components.BookItem
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.ListButton
import aragones.sergio.readercollection.presentation.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.search.reachedBottom
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@Composable
fun BookListScreen(
    state: BookListUiState,
    onBookClick: (String) -> Unit,
    onBack: () -> Unit,
    onDragClick: () -> Unit,
    onSortClick: () -> Unit,
    onDrag: (List<Book>) -> Unit,
    onDragEnd: (List<Book>) -> Unit,
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

    val title = if (state is BookListUiState.Success && state.books.isNotEmpty()) {
        pluralStringResource(
            R.plurals.title_books_count,
            state.books.size,
            state.books.size,
        )
    } else {
        ""
    }
    val subtitle = when (state) {
        is BookListUiState.Success -> state.subtitle
        BookListUiState.Empty -> ""
    }
    val actions: @Composable RowScope.() -> Unit = when (state) {
        is BookListUiState.Success -> {
            {
                if (state.books.any { it.isPending() }) {
                    TopAppBarIcon(
                        icon = if (state.isDraggingEnabled) {
                            R.drawable.ic_disable_drag
                        } else {
                            R.drawable.ic_enable_drag
                        },
                        onClick = onDragClick,
                    )
                } else {
                    TopAppBarIcon(
                        icon = R.drawable.ic_sort_books,
                        onClick = onSortClick,
                    )
                }
            }
        }
        else -> {
            {}
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CustomToolbar(
            title = title,
            modifier = Modifier.shadow(if (showTopButton) 4.dp else 0.dp),
            subtitle = subtitle,
            backgroundColor = MaterialTheme.colorScheme.background,
            onBack = onBack,
            actions = actions,
        )
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            when (state) {
                is BookListUiState.Empty -> {
                    onBack()
                }
                is BookListUiState.Success -> {
                    if (state.books.isEmpty()) {
                        NoResultsComponent()
                    } else {
                        BookListContent(
                            state = state,
                            listState = listState,
                            showTopButton = showTopButton,
                            showBottomButton = showBottomButton,
                            onBookClick = onBookClick,
                            onDrag = onDrag,
                            onDragEnd = onDragEnd,
                        )
                    }
                }
            }

            if (state is BookListUiState.Success && state.isLoading) {
                CustomCircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun BookListContent(
    state: BookListUiState.Success,
    listState: LazyListState,
    showTopButton: Boolean,
    showBottomButton: Boolean,
    onBookClick: (String) -> Unit,
    onDrag: (List<Book>) -> Unit,
    onDragEnd: (List<Book>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val books = state.books.toMutableStateList()
    val dragAndDropListState =
        rememberDragAndDropListState(listState) { from, to ->
            books.move(from, to)
            onDrag(books)
        }

    val coroutineScope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }

    var draggingIndex by remember { mutableIntStateOf(-1) }

    val draggingModifier = if (state.isDraggingEnabled) {
        Modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, offset ->
                    change.consume()
                    dragAndDropListState.onDrag(offset)

                    if (overscrollJob?.isActive == true) return@detectDragGesturesAfterLongPress

                    dragAndDropListState
                        .checkOverscroll()
                        .takeIf { it != 0f }
                        ?.let {
                            overscrollJob = coroutineScope.launch {
                                dragAndDropListState.lazyListState.scrollBy(it)
                            }
                        } ?: kotlin.run { overscrollJob?.cancel() }

                    draggingIndex = dragAndDropListState.currentIndexOfDraggedItem ?: -1
                },
                onDragStart = { offset ->
                    dragAndDropListState.onDragStart(offset)
                    draggingIndex = dragAndDropListState.currentIndexOfDraggedItem ?: -1
                },
                onDragEnd = {
                    dragAndDropListState.onDragInterrupted()
                    onDragEnd(books)
                    draggingIndex = -1
                },
                onDragCancel = {
                    dragAndDropListState.onDragInterrupted()
                    draggingIndex = -1
                },
            )
        }
    } else {
        Modifier
    }

    val topOffset by animateFloatAsState(
        targetValue = if (showTopButton) 0f else 250f,
        label = "",
    )
    val bottomOffset by animateFloatAsState(
        targetValue = if (showBottomButton) 0f else 250f,
        label = "",
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = dragAndDropListState.lazyListState,
            modifier = draggingModifier,
        ) {
            itemsIndexed(books) { index, book ->
                BookItem(
                    book = book,
                    onBookClick = onBookClick,
                    modifier = Modifier
                        .composed {
                            val offset =
                                dragAndDropListState.elementDisplacement.takeIf {
                                    index == dragAndDropListState.currentIndexOfDraggedItem
                                } ?: 0f
                            Modifier.graphicsLayer {
                                translationY = offset
                            }
                        }.zIndex(1f.takeIf { draggingIndex == index } ?: 0f),
                    showDivider = index < books.size - 1,
                    isDraggingEnabled = state.isDraggingEnabled,
                    isDragging = index == draggingIndex,
                )
            }
        }
        ListButton(
            image = R.drawable.ic_double_arrow_up,
            onClick = {
                coroutineScope.launch {
                    dragAndDropListState.lazyListState.animateScrollToItem(index = 0)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(topOffset.toInt(), 0) },
        )
        ListButton(
            image = R.drawable.ic_double_arrow_down,
            onClick = {
                coroutineScope.launch {
                    dragAndDropListState.lazyListState.animateScrollToItem(
                        index = listState.layoutInfo.totalItemsCount - 1,
                    )
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset { IntOffset(bottomOffset.toInt(), 0) },
        )
    }
}

private fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val element = this.removeAt(from)
    this.add(to, element)
}

@CustomPreviewLightDark
@Composable
private fun BookListScreenPreview(
    @PreviewParameter(BookListScreenPreviewParameterProvider::class) state: BookListUiState,
) {
    ReaderCollectionTheme {
        BookListScreen(
            state = state,
            onBookClick = {},
            onBack = {},
            onDragClick = {},
            onSortClick = {},
            onDrag = {},
            onDragEnd = {},
        )
    }
}

private class BookListScreenPreviewParameterProvider :
    PreviewParameterProvider<BookListUiState> {

    override val values: Sequence<BookListUiState>
        get() = sequenceOf(
            BookListUiState.Success(
                isLoading = false,
                books = listOf(
                    Book(
                        id = "1",
                        title = "Title 1",
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
                        state = BookState.PENDING,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Title 2",
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
                        state = BookState.PENDING,
                        priority = 0,
                    ),
                ),
                subtitle = "",
                isDraggingEnabled = false,
            ),
            BookListUiState.Success(
                isLoading = true,
                books = listOf(
                    Book(
                        id = "1",
                        title = "Large title for stored book in the list that should not be shown",
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
                        state = BookState.READ,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Title",
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
                        state = BookState.READ,
                        priority = 0,
                    ),
                ),
                subtitle = "2025",
                isDraggingEnabled = true,
            ),
        )
}
