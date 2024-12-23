/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/5/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.description
import aragones.sergio.readercollection.presentation.ui.theme.roseBud
import aragones.sergio.readercollection.presentation.ui.theme.selector

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
                    painter = painterResource(id = R.drawable.ic_enable_drag),
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
            Spacer(Modifier.width(24.dp))
            ImageWithLoading(
                imageUrl = book.thumbnail,
                placeholder = R.drawable.ic_default_book_cover_blue,
                modifier = Modifier
                    .widthIn(max = 130.dp)
                    .fillMaxHeight(),
                shape = MaterialTheme.shapes.medium,
            )
            Spacer(Modifier.width(16.dp))
            BookInfo(
                book = book,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
            )
            Spacer(Modifier.width(24.dp))
        }
        if (showDivider) {
            Divider(
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
                text = stringResource(id = R.string.new_book),
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeItem(
    direction: DismissDirection,
    dismissValue: DismissValue,
    threshold: Float,
    onSwipe: () -> Unit,
    background: @Composable RowScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val swipeState = rememberDismissState(
        confirmStateChange = {
            if (it == dismissValue) onSwipe()
            false
        },
    )
    SwipeToDismiss(
        state = swipeState,
        directions = setOf(direction),
        dismissThresholds = {
            FractionalThreshold(threshold)
        },
        background = background,
        dismissContent = content,
    )
}

@Composable
fun SwipeItemBackground(dismissValue: DismissValue, color: Color, icon: Int? = null) {
    Row(modifier = Modifier.fillMaxSize()) {
        val alignment = when (dismissValue) {
            DismissValue.Default -> Alignment.Center
            DismissValue.DismissedToEnd -> Alignment.CenterStart
            DismissValue.DismissedToStart -> Alignment.CenterEnd
        }
        if (dismissValue == DismissValue.DismissedToStart) {
            Spacer(modifier = Modifier.fillMaxWidth(.1f))
        }
        Box(
            modifier = Modifier
                .background(color)
                .fillMaxHeight()
                .run {
                    if (dismissValue == DismissValue.DismissedToEnd) {
                        fillMaxWidth(0.9f)
                    } else {
                        fillMaxWidth()
                    }
                },
            contentAlignment = alignment,
        ) {
            if (icon != null) {
                Icon(
                    painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(48.dp),
                )
            }
        }
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun BookItemPreview() {
    ReaderCollectionTheme {
        BookItem(
            book = Book(
                "1",
                "Book title with a very very very very very very very very long text",
                null,
                listOf("Author with a very long name"),
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
                7.0,
                null,
                null,
                null,
                null,
                false,
                0,
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
                "1",
                "Book title with a very very very very very very very very long text",
                null,
                listOf("Author with a very long name"),
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
            onBookClick = {},
            isDraggingEnabled = true,
            isDragging = true,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun SwipeItemToLeftPreview() {
    ReaderCollectionTheme {
        SwipeItem(
            direction = DismissDirection.EndToStart,
            dismissValue = DismissValue.DismissedToStart,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    dismissValue = DismissValue.DismissedToStart,
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
            direction = DismissDirection.StartToEnd,
            dismissValue = DismissValue.DismissedToEnd,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    dismissValue = DismissValue.DismissedToEnd,
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