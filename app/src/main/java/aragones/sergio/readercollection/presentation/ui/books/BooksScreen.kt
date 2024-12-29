/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/12/2024
 */

package aragones.sergio.readercollection.presentation.ui.books

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.ReadingBookItem
import aragones.sergio.readercollection.presentation.ui.components.SearchBar
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.components.VerticalBookItem
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants

@Composable
fun BooksScreen(
    state: BooksUiState,
    onSortClick: () -> Unit,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onShowAll: (String) -> Unit,
    onSwitchToLeft: (Int) -> Unit,
    onSwitchToRight: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = when(state) {
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CustomToolbar(
            title = stringResource(R.string.title_books),
            modifier = Modifier.background(MaterialTheme.colors.background),
            subtitle = subtitle,
            actions = {
                TopAppBarIcon(
                    icon = R.drawable.ic_sort_books,
                    onClick = onSortClick,
                )
            },
        )
        Spacer(Modifier.height(16.dp))
        SearchBar(
            text = state.query,
            onSearch = onSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            showLeadingIcon = true,
            requestFocusByDefault = false,
        )
        Spacer(Modifier.height(16.dp))
        when (state) {
            is BooksUiState.Empty -> NoResultsComponent()
            is BooksUiState.Success -> BooksScreenContent(
                books = state.books,
                isSwitchingEnabled = state.query.isBlank(),
                onBookClick = onBookClick,
                onShowAll = onShowAll,
                onSwitchToLeft = onSwitchToLeft,
                onSwitchToRight = onSwitchToRight,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(24.dp))
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun BooksScreenContent(
    books: List<Book>,
    isSwitchingEnabled: Boolean,
    onBookClick: (String) -> Unit,
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
                modifier = Modifier
                    .height(275.dp)
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
            )
        }
    }
}

@Composable
private fun ReadingBooksSection(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        if (books.isNotEmpty()) {
            ReadingBooksContentSection(
                books = books,
                onBookClick = onBookClick,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.image_user_reading),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.5f)
                    .weight(1f)
                    .align(Alignment.CenterHorizontally),
            )
        }
        Spacer(Modifier.height(16.dp))
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = MaterialTheme.colors.primaryVariant,
        )
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ReadingBooksContentSection(
    books: List<Book>,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    LazyRow(modifier) {
        items(books) { book ->
            ReadingBookItem(
                book = book,
                onBookClick = onBookClick,
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
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
) {
    if (books.isNotEmpty()) {
        Column(modifier) {
            BooksSectionHeader(title = title, onShowAll = onShowAll)
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
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    color = MaterialTheme.colors.primaryVariant,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BooksSectionHeader(title: String, onShowAll: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
        TextButton(onClick = onShowAll) {
            Text(
                text = stringResource(id = R.string.show_all),
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.primary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ShowAllItems(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .width(150.dp)
            .height(200.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_circle_right),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
        Text(
            text = stringResource(R.string.show_all),
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
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
        )
    }
}

private class BooksScreenPreviewParameterProvider : PreviewParameterProvider<BooksUiState> {

    override val values: Sequence<BooksUiState>
        get() = sequenceOf(
            BooksUiState.Success(
                books = listOf(
                    Book(
                        "1",
                        "Reading book",
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
                        BookState.READING,
                        false,
                        0,
                    ),
                    Book(
                        "2",
                        "Reading book with large title",
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
                        BookState.READING,
                        false,
                        0,
                    ),
                    Book(
                        "3",
                        "Pending book",
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
                        0,
                    ),
                    Book(
                        "4",
                        "Pending book with large title",
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
                        0.0,
                        null,
                        null,
                        null,
                        BookState.PENDING,
                        false,
                        0,
                    ),
                    Book(
                        "5",
                        "Read book with large title",
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
                        0,
                    ),
                    Book(
                        "6",
                        "Read book",
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
                        0,
                    ),
                ),
                isLoading = false,
                query = "",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        "1",
                        "Pending book",
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
                        0,
                    ),
                    Book(
                        "2",
                        "Read book",
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
                        0,
                    ),
                ),
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        "1",
                        "Reading book",
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
                        BookState.READING,
                        false,
                        0,
                    ),
                    Book(
                        "2",
                        "Read book",
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
                        0,
                    ),
                ),
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Success(
                books = listOf(
                    Book(
                        "1",
                        "Reading book",
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
                        BookState.READING,
                        false,
                        0,
                    ),
                    Book(
                        "2",
                        "Pending book",
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
                        0,
                    ),
                ),
                isLoading = true,
                query = "text",
            ),
            BooksUiState.Empty("", false),
            BooksUiState.Empty("text", true),
        )
}