/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.presentation.components.ButtonType
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.VerticalBookItem
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.delete
import reader_collection.app.generated.resources.pending
import reader_collection.app.generated.resources.read
import reader_collection.app.generated.resources.reading

@Composable
fun FriendDetailScreen(
    state: FriendDetailUiState,
    onBack: () -> Unit,
    onBookClick: (String, String) -> Unit,
    onDeleteFriend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        FriendDetailScreenToolbar(onBack = onBack)
        when (state) {
            FriendDetailUiState.Loading -> {
                CustomCircularProgressIndicator()
            }
            is FriendDetailUiState.Success -> {
                FriendDetailContent(
                    friend = state.friend,
                    books = state.books,
                    onBookClick = { onBookClick(it, state.friend.id) },
                    onDeleteFriend = { onDeleteFriend(state.friend.id) },
                )
            }
        }
    }
}

@Composable
private fun FriendDetailScreenToolbar(onBack: (() -> Unit)) {
    CustomToolbar(
        title = "",
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun FriendDetailContent(
    friend: User,
    books: Books,
    onBookClick: (String) -> Unit,
    onDeleteFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val readingTitle = stringResource(Res.string.reading)
    val readTitle = stringResource(Res.string.read)
    val pendingTitle = stringResource(Res.string.pending)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            UserInfo(user = friend)
        }
        item(span = { GridItemSpan(2) }) {
            Row {
                Spacer(Modifier.weight(1f))
                MainActionButton(
                    text = stringResource(Res.string.delete),
                    modifier = Modifier
                        .widthIn(min = 200.dp)
                        .padding(vertical = 24.dp),
                    enabled = true,
                    onClick = onDeleteFriend,
                    type = ButtonType.DESTRUCTIVE,
                )
                Spacer(Modifier.weight(1f))
            }
        }
        BooksSection(
            title = readingTitle,
            books = books.books.filter { it.isReading() },
            onBookClick = onBookClick,
        )
        BooksSection(
            title = readTitle,
            books = books.books
                .filter { !it.isReading() && !it.isPending() }
                .sortedBy { it.readingDate }
                .reversed(),
            onBookClick = onBookClick,
        )
        BooksSection(
            title = pendingTitle,
            books = books.books
                .filter { it.isPending() }
                .sortedBy { it.priority },
            onBookClick = onBookClick,
        )
        item {
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun UserInfo(user: User) {
    Column {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(150.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = user.username,
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun LazyGridScope.BooksSection(
    title: String,
    books: List<Book>,
    onBookClick: (String) -> Unit,
) {
    if (books.isNotEmpty()) {
        stickyHeader {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = title,
                    modifier = Modifier.semantics { heading() },
                    textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        }
        items(
            items = books,
            key = { book -> book.id },
        ) { book ->
            VerticalBookItem(
                book = book,
                isSwitchLeftIconEnabled = false,
                isSwitchRightIconEnabled = false,
                onClick = { onBookClick(book.id) },
                onSwitchToLeft = {},
                onSwitchToRight = {},
                onLongClick = {},
            )
        }
        item(span = { GridItemSpan(2) }) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun FriendDetailScreenPreview(
    @PreviewParameter(FriendDetailScreenPreviewParameterProvider::class) state: FriendDetailUiState,
) {
    ReaderCollectionTheme {
        FriendDetailScreen(
            state = state,
            onBack = {},
            onBookClick = { _, _ -> },
            onDeleteFriend = {},
        )
    }
}

private class FriendDetailScreenPreviewParameterProvider :
    PreviewParameterProvider<FriendDetailUiState> {

    override val values: Sequence<FriendDetailUiState>
        get() = sequenceOf(
            FriendDetailUiState.Success(
                friend = User(
                    id = "1",
                    username = "User 1",
                    status = RequestStatus.APPROVED,
                ),
                books = Books(
                    listOf(
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
                            state = BookState.READING,
                            priority = 0,
                        ),
                        Book(
                            id = "3",
                            title = "Title 3",
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
                            id = "4",
                            title = "Title 4",
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
                            id = "5",
                            title = "Title 5",
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
                            state = BookState.READING,
                            priority = 0,
                        ),
                    ),
                ),
            ),
            FriendDetailUiState.Success(
                friend = User(
                    id = "2",
                    username = "User 2",
                    status = RequestStatus.APPROVED,
                ),
                books = Books(
                    listOf(
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
                    ),
                ),
            ),
            FriendDetailUiState.Success(
                friend = User(
                    id = "3",
                    username = "User 3",
                    status = RequestStatus.APPROVED,
                ),
                books = Books(),
            ),
            FriendDetailUiState.Loading,
        )
}