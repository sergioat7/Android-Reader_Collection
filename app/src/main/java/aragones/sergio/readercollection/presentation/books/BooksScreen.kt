/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/12/2024
 */

package aragones.sergio.readercollection.presentation.books

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomFilterChip
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.ListButton
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.components.ReadingBookItem
import aragones.sergio.readercollection.presentation.components.SearchBar
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.components.VerticalBookItem
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.utils.Constants as MyConstants
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(
    state: BooksUiState,
    onSortClick: () -> Unit,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onShowAll: (String) -> Unit,
    onSwitchToLeft: (Int) -> Unit,
    onSwitchToRight: (Int) -> Unit,
    onBookStateChange: (Book) -> Unit,
    onAddBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        BooksScreenContent(
            state = state,
            onSortClick = onSortClick,
            onSearch = onSearch,
            onBookClick = onBookClick,
            onLongClickBook = { book ->
                selectedBook = book
                coroutineScope.launch {
                    sheetState.show()
                }
            },
            onShowAll = onShowAll,
            onSwitchToLeft = onSwitchToLeft,
            onSwitchToRight = onSwitchToRight,
        )
        ListButton(
            painter = painterResource(R.drawable.ic_save_book)
                .withDescription(stringResource(R.string.go_to_add_new_book)),
            onClick = onAddBook,
            modifier = Modifier.align(Alignment.BottomEnd),
        )
    }
    if (sheetState.isVisible) {
        ModalBottomSheet(
            onDismissRequest = {},
            modifier = modifier,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            selectedBook?.let { book ->
                BottomSheetContent(
                    book = book,
                    onStateClick = { newState ->
                        if (newState != book.state) {
                            val modifiedBook = book.copy(state = newState)
                            onBookStateChange(modifiedBook)
                            selectedBook = modifiedBook
                        }
                    },
                    onDone = {
                        coroutineScope.launch {
                            sheetState.hide()
                            selectedBook = null
                        }
                    },
                )
            }
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun BooksScreenContent(
    state: BooksUiState,
    onSortClick: () -> Unit,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onLongClickBook: (Book) -> Unit,
    onShowAll: (String) -> Unit,
    onSwitchToLeft: (Int) -> Unit,
    onSwitchToRight: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = when (state) {
        is BooksUiState.Empty -> pluralStringResource(
            R.plurals.title_books_count,
            0,
            0,
        )
        is BooksUiState.Success -> pluralStringResource(
            R.plurals.title_books_count,
            state.books.size,
            state.books.size,
        )
    }

    Column(modifier = modifier) {
        CustomToolbar(
            title = stringResource(R.string.title_books),
            subtitle = subtitle,
            backgroundColor = MaterialTheme.colorScheme.background,
            actions = {
                TopAppBarIcon(
                    accessibilityPainter = painterResource(R.drawable.ic_sort_books)
                        .withDescription(stringResource(R.string.sort_books)),
                    onClick = onSortClick,
                )
            },
        )
        Spacer(Modifier.height(16.dp))
        SearchBar(
            text = state.query,
            onSearch = onSearch,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            showLeadingIcon = true,
            requestFocusByDefault = false,
        )
        Spacer(Modifier.height(16.dp))
        when (state) {
            is BooksUiState.Empty -> NoResultsComponent(
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
            is BooksUiState.Success -> BooksComponent(
                books = state.books,
                isSwitchingEnabled = state.query.isBlank(),
                onBookClick = onBookClick,
                onLongClickBook = onLongClickBook,
                onShowAll = onShowAll,
                onSwitchToLeft = onSwitchToLeft,
                onSwitchToRight = onSwitchToRight,
                modifier = Modifier.fillMaxSize().semantics {
                    liveRegion = LiveRegionMode.Polite
                },
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BooksComponent(
    books: List<Book>,
    isSwitchingEnabled: Boolean,
    onBookClick: (String) -> Unit,
    onLongClickBook: (Book) -> Unit,
    onShowAll: (String) -> Unit,
    onSwitchToLeft: (Int) -> Unit,
    onSwitchToRight: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val readingBooks = books.filter { it.isReading() }
    val pendingBooks = books.filter { it.isPending() }.sortedBy { it.priority }
    val readBooks = books.filter { !it.isReading() && !it.isPending() }

    LazyColumn(modifier) {
        item {
            ReadingBooksSection(
                books = readingBooks,
                onBookClick = onBookClick,
                onLongClick = onLongClickBook,
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
            )
        }
        item {
            BooksSection(
                title = stringResource(R.string.pending),
                books = pendingBooks.take(Constants.BOOKS_TO_SHOW),
                isSwitchingEnabled = isSwitchingEnabled,
                showAll = pendingBooks.size > Constants.BOOKS_TO_SHOW,
                onShowAll = {
                    onShowAll(BookState.PENDING)
                },
                onBookClick = onBookClick,
                onSwitchToLeft = onSwitchToLeft,
                onSwitchToRight = onSwitchToRight,
                onLongClickBook = onLongClickBook,
            )
        }
        item {
            BooksSection(
                title = stringResource(R.string.read),
                books = readBooks.take(Constants.BOOKS_TO_SHOW),
                isSwitchingEnabled = isSwitchingEnabled,
                showAll = readBooks.size > Constants.BOOKS_TO_SHOW,
                onShowAll = {
                    onShowAll(BookState.READ)
                },
                onBookClick = onBookClick,
                showDivider = false,
                onSwitchToLeft = {},
                onSwitchToRight = {},
                onLongClickBook = onLongClickBook,
            )
        }
    }
}

@Composable
private fun ReadingBooksSection(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    onLongClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (books.isNotEmpty()) {
            ReadingBooksContentSection(
                books = books,
                onBookClick = onBookClick,
                onLongClick = onLongClick,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        } else {
            Image(
                painter = painterResource(R.drawable.image_user_reading),
                contentDescription = stringResource(R.string.not_reading_anything_yet),
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .weight(1f)
                    .align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.height(16.dp))
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.tertiary,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ReadingBooksContentSection(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    onLongClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    LazyRow(modifier) {
        items(books) { book ->
            ReadingBookItem(
                book = book,
                onBookClick = onBookClick,
                onLongClick = onLongClick,
                modifier = Modifier.width(screenWidthDp.dp),
            )
        }
    }
}

@Composable
private fun BooksSection(
    title: String,
    books: List<Book>,
    isSwitchingEnabled: Boolean,
    showAll: Boolean,
    onShowAll: () -> Unit,
    onBookClick: (String) -> Unit,
    onSwitchToLeft: (Int) -> Unit,
    onSwitchToRight: (Int) -> Unit,
    onLongClickBook: (Book) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    if (books.isNotEmpty()) {
        Column(modifier) {
            BooksSectionHeader(
                title = title,
                showAll = showAll,
                onShowAll = onShowAll,
            )
            Spacer(Modifier.height(8.dp))
            LazyRow {
                itemsIndexed(books) { index, book ->

                    val isFirst = index == 0 || !isSwitchingEnabled
                    val isLast = index == Constants.BOOKS_TO_SHOW - 1 ||
                        index == books.count() - 1 ||
                        !isSwitchingEnabled
                    VerticalBookItem(
                        book = book,
                        isSwitchLeftIconEnabled = !isFirst && book.isPending(),
                        isSwitchRightIconEnabled = !isLast && book.isPending(),
                        onClick = { onBookClick(book.id) },
                        onSwitchToLeft = {
                            onSwitchToLeft(index)
                        },
                        onSwitchToRight = {
                            onSwitchToRight(index)
                        },
                        onLongClick = { onLongClickBook(book) },
                    )
                }
                if (showAll) {
                    item {
                        ShowAllItems(onClick = onShowAll)
                    }
                }
            }
            if (showDivider) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BooksSectionHeader(title: String, showAll: Boolean, onShowAll: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        if (showAll) {
            TextButton(onClick = onShowAll) {
                Text(
                    text = stringResource(R.string.show_all),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ShowAllItems(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val text = stringResource(R.string.show_all)
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .width(150.dp)
            .height(200.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = text
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_arrow_circle_right),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
private fun BottomSheetContent(book: Book, onStateClick: (String?) -> Unit, onDone: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = book.title ?: "",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(8.dp))
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.tertiary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            for (state in MyConstants.STATES) {
                CustomFilterChip(
                    title = state.name,
                    selected = state.id == book.state,
                    onClick = { onStateClick(state.id) },
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.tertiary,
                    ),
                    selectedIcon = rememberVectorPainter(Icons.Default.Done)
                        .withDescription(null)
                        .takeIf { state.id == book.state },
                )
            }
        }
        MainActionButton(
            text = stringResource(R.string.accept),
            modifier = Modifier.fillMaxWidth(),
            enabled = true,
            onClick = onDone,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@CustomPreviewLightDark
@Composable
fun BooksScreenPreview(
    @PreviewParameter(BooksScreenPreviewParameterProvider::class) state: BooksUiState,
) {
    ReaderCollectionTheme {
        BooksScreen(
            state = state,
            onSortClick = {},
            onSearch = {},
            onBookClick = {},
            onShowAll = {},
            onSwitchToLeft = {},
            onSwitchToRight = {},
            onBookStateChange = {},
            onAddBook = {},
        )
    }
}

private class BooksScreenPreviewParameterProvider : PreviewParameterProvider<BooksUiState> {

    override val values: Sequence<BooksUiState>
        get() = sequenceOf(
            BooksUiState.Success(
                books = listOf(
                    Book(
                        id = "1",
                        title = "Reading book",
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
                        state = BookState.READING,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Reading book with large title",
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
                        state = BookState.READING,
                        priority = 0,
                    ),
                    Book(
                        id = "3",
                        title = "Pending book",
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
                    Book(
                        id = "4",
                        title = "Pending book with large title",
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
                        rating = 0.0,
                        thumbnail = null,
                        image = null,
                        format = null,
                        state = BookState.PENDING,
                        priority = 0,
                    ),
                    Book(
                        id = "5",
                        title = "Read book with large title",
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
                    Book(
                        id = "6",
                        title = "Read book",
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
                isLoading = false,
                query = "",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        id = "1",
                        title = "Pending book",
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
                        title = "Read book",
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
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        id = "1",
                        title = "Reading book",
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
                        state = BookState.READING,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Read book",
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
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        id = "1",
                        title = "Reading book",
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
                        state = BookState.READING,
                        priority = 0,
                    ),
                    Book(
                        id = "2",
                        title = "Pending book",
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
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Empty(query = "", isLoading = false),
            BooksUiState.Empty(query = "text", isLoading = true),
        )
}