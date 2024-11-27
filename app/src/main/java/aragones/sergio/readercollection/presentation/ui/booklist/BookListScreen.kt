/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 3/9/2024
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.BookItem
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.ListButton
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.search.reachedBottom
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
            state.books.size
        )
    } else ""
    val actions: @Composable RowScope.() -> Unit = when (state) {
        is BookListUiState.Success -> {
            {
                if (state.books.any { it.isPending() }) {
                    TopAppBarIcon(
                        icon = if (state.isDraggingEnabled) R.drawable.ic_disable_drag else R.drawable.ic_enable_drag,
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
            actions = actions
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
                                dragAndDropListState.lazyListState.scrollBy(
                                    it
                                )
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
                }
            )
        }
    } else Modifier

    val topOffset by animateFloatAsState(
        targetValue = if (showTopButton) 0f else 200f,
        label = ""
    )
    val bottomOffset by animateFloatAsState(
        targetValue = if (showBottomButton) 0f else 200f,
        label = ""
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
                    modifier = Modifier.composed {
                        val offset =
                            dragAndDropListState.elementDisplacement.takeIf {
                                index == dragAndDropListState.currentIndexOfDraggedItem
                            } ?: 0f
                        Modifier.graphicsLayer {
                            translationY = offset
                        }
                    },
                    showDivider = index < books.size - 1,
                    isDraggingEnabled = state.isDraggingEnabled,
                    isDragging = index == draggingIndex,
                )
            }
        }
        ListButton(
            image = R.drawable.ic_double_arrow_up,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(topOffset.toInt(), 0) },
            onClick = {
                coroutineScope.launch {
                    dragAndDropListState.lazyListState.animateScrollToItem(index = 0)
                }
            },
        )
        ListButton(
            image = R.drawable.ic_double_arrow_down,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset { IntOffset(bottomOffset.toInt(), 0) },
            onClick = {
                coroutineScope.launch {
                    dragAndDropListState.lazyListState.animateScrollToItem(index = listState.layoutInfo.totalItemsCount - 1)
                }
            },
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
        onDrag = {},
        onDragEnd = {},
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
        onDrag = {},
        onDragEnd = {},
    )
}

fun <T> MutableList<T>.move(from: Int, to: Int) {
    if (from == to) return
    val element = this.removeAt(from)
    this.add(to, element)
}
