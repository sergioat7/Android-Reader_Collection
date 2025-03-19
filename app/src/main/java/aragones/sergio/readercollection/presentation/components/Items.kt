/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/5/2024
 */

@file:OptIn(ExperimentalFoundationApi::class)

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import aragones.sergio.readercollection.presentation.theme.roseBud
import aragones.sergio.readercollection.presentation.theme.selector
import com.aragones.sergio.util.extensions.isNotBlank

@Composable
fun BookItem(
    book: Book,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    isDraggingEnabled: Boolean = false,
    isDragging: Boolean = false,
) {
    Column(
        modifier = modifier
            .background(
                if (isDragging) {
                    MaterialTheme.colors.selector
                } else {
                    MaterialTheme.colors.background
                },
            ).fillMaxWidth()
            .height(220.dp)
            .clickable {
                onBookClick(book.id)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp)
                .weight(1f),
        ) {
            if (isDraggingEnabled) {
                Spacer(Modifier.width(24.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_enable_drag),
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
            ImageWithLoading(
                imageUrl = book.thumbnail,
                placeholder = if (MaterialTheme.colors.isLight) {
                    R.drawable.ic_default_book_cover_blue
                } else {
                    R.drawable.ic_default_book_cover_white
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 115.dp)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
                contentScale = ContentScale.FillWidth,
            )
            BookInfo(
                book = book,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
            Spacer(Modifier.width(24.dp))
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                color = MaterialTheme.colors.primaryVariant,
            )
        }
    }
}

@Composable
private fun BookInfo(book: Book, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = book.title ?: "",
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
        )
        if (book.authorsToString().isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = book.authorsToString(),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.description,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (book.rating > 0) {
            RatingStars(
                rating = book.rating,
                modifier = Modifier.height(30.dp),
            )
        } else {
            Text(
                text = stringResource(R.string.new_book),
                style = MaterialTheme.typography.h1,
                color = MaterialTheme.colors.roseBud,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RatingStars(rating: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StarRatingBar(
            rating = rating.toFloat() / 2,
            onRatingChanged = {},
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = rating.toInt().toString(),
            style = MaterialTheme.typography.h2,
            color = MaterialTheme.colors.roseBud,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
fun ReadingBookItem(
    book: Book,
    onBookClick: (String) -> Unit,
    onLongClick: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    onBookClick(book.id)
                },
                onLongClick = {
                    onLongClick(book)
                },
            ),
    ) {
        BookBasicInfo(title = book.title ?: "", subtitle = book.authorsToString())
        Spacer(Modifier.height(8.dp))
        ImageWithLoading(
            imageUrl = book.thumbnail,
            placeholder = if (MaterialTheme.colors.isLight) {
                R.drawable.ic_default_book_cover_blue
            } else {
                R.drawable.ic_default_book_cover_white
            },
            contentScale = ContentScale.FillWidth,
            shape = MaterialTheme.shapes.small,
        )
    }
}

@Composable
private fun BookBasicInfo(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.h3,
        color = MaterialTheme.colors.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
    )
    if (subtitle.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.body2,
            color = MaterialTheme.colors.description,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
fun VerticalBookItem(
    book: Book,
    isSwitchLeftIconEnabled: Boolean,
    isSwitchRightIconEnabled: Boolean,
    onClick: () -> Unit,
    onSwitchToLeft: () -> Unit,
    onSwitchToRight: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .width(160.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (isSwitchLeftIconEnabled) {
                IconButton(onClick = onSwitchToLeft) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_switch_left),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (isSwitchRightIconEnabled) {
                IconButton(onClick = onSwitchToRight) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_switch_right),
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
        ) {
            ImageWithLoading(
                imageUrl = book.thumbnail,
                placeholder = if (MaterialTheme.colors.isLight) {
                    R.drawable.ic_default_book_cover_blue
                } else {
                    R.drawable.ic_default_book_cover_white
                },
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(8.dp))
            BookBasicInfo(title = book.title ?: "", subtitle = book.authorsToString())
        }
    }
}

@Composable
fun SwipeItem(
    direction: SwipeDirection,
    dismissValue: SwipeToDismissBoxValue,
    threshold: Float,
    onSwipe: () -> Unit,
    background: @Composable RowScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == dismissValue) onSwipe()
            false
        },
        positionalThreshold = { it * threshold },
    )
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = direction == SwipeDirection.RIGHT,
        enableDismissFromEndToStart = direction == SwipeDirection.LEFT,
        backgroundContent = background,
        content = content,
    )
}

@Composable
fun SwipeItemBackground(dismissValue: SwipeToDismissBoxValue, color: Color, icon: Int? = null) {
    Row(modifier = Modifier.fillMaxSize()) {
        val alignment = when (dismissValue) {
            SwipeToDismissBoxValue.Settled -> Alignment.Center
            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        }
        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
            Spacer(modifier = Modifier.fillMaxWidth(.1f))
        }
        Box(
            modifier = Modifier
                .background(color)
                .fillMaxHeight()
                .run {
                    if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                        fillMaxWidth(0.9f)
                    } else {
                        fillMaxWidth()
                    }
                },
            contentAlignment = alignment,
        ) {
            if (icon != null) {
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(48.dp),
                )
            }
        }
    }
}

enum class SwipeDirection {
    LEFT,
    RIGHT,
    NONE,
}

@PreviewLightDarkWithBackground
@Composable
private fun BookItemPreview() {
    ReaderCollectionTheme {
        BookItem(
            book = Book(
                id = "1",
                title = "Book title with a very very very very very very very very long text",
                subtitle = null,
                authors = listOf("Author with a very long name"),
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
                rating = 7.0,
                thumbnail = null,
                image = null,
                format = null,
                state = null,
                priority = 0,
            ),
            onBookClick = {},
            isDraggingEnabled = false,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun BookItemWithDraggingPreview() {
    ReaderCollectionTheme {
        BookItem(
            book = Book(
                id = "1",
                title = "Book title with a very very very very very very very very long text",
                subtitle = null,
                authors = listOf("Author with a very long name"),
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
            onBookClick = {},
            isDraggingEnabled = true,
            isDragging = true,
        )
    }
}

@PreviewLightDark
@Composable
private fun ReadingBookItemPreview() {
    ReaderCollectionTheme {
        ReadingBookItem(
            book = Book(
                id = "1",
                title = "Book title with a very very very very very very very very long text",
                subtitle = null,
                authors = listOf("Author with a very long name"),
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
            onBookClick = {},
            onLongClick = {},
            modifier = Modifier.height(250.dp),
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun VerticalBookItemPreview() {
    ReaderCollectionTheme {
        VerticalBookItem(
            book = Book(
                id = "1",
                title = "Book title with a very very very very very very very very long text",
                subtitle = null,
                authors = listOf("Author with a very long name"),
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
            isSwitchLeftIconEnabled = true,
            isSwitchRightIconEnabled = true,
            onClick = {},
            onSwitchToLeft = {},
            onSwitchToRight = {},
            onLongClick = {},
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun SwipeItemToLeftPreview() {
    ReaderCollectionTheme {
        SwipeItem(
            direction = SwipeDirection.LEFT,
            dismissValue = SwipeToDismissBoxValue.EndToStart,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    dismissValue = SwipeToDismissBoxValue.EndToStart,
                    color = MaterialTheme.colors.roseBud,
                    icon = R.drawable.ic_save_book,
                )
            },
            content = {
                Box(Modifier.size(200.dp))
            },
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun SwipeItemToRightPreview() {
    ReaderCollectionTheme {
        SwipeItem(
            direction = SwipeDirection.RIGHT,
            dismissValue = SwipeToDismissBoxValue.StartToEnd,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    dismissValue = SwipeToDismissBoxValue.StartToEnd,
                    color = MaterialTheme.colors.roseBud,
                    icon = R.drawable.ic_remove_book,
                )
            },
            content = {
                Box(Modifier.size(200.dp))
            },
        )
    }
}