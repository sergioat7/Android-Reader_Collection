/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/5/2024
 */

package aragones.sergio.readercollection.presentation.components

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.isLight
import aragones.sergio.readercollection.presentation.theme.roseBud
import aragones.sergio.readercollection.presentation.theme.selector
import com.aragones.sergio.util.extensions.isNotBlank
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.decrease_priority_description
import reader_collection.app.generated.resources.dragging_enabled_description
import reader_collection.app.generated.resources.increase_priority_description
import reader_collection.app.generated.resources.new_book
import reader_collection.app.generated.resources.no_rated_description

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
                    MaterialTheme.colorScheme.selector
                } else {
                    MaterialTheme.colorScheme.background
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
                    contentDescription = stringResource(Res.string.dragging_enabled_description),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
            }
            ImageWithLoading(
                imageUrl = book.thumbnail,
                placeholder = if (MaterialTheme.colorScheme.isLight()) {
                    R.drawable.ic_default_book_cover_blue
                } else {
                    R.drawable.ic_default_book_cover_white
                },
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .widthIn(max = 115.dp)
                    .fillMaxHeight(),
                contentDescription = book.title,
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
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
private fun BookInfo(book: Book, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = book.title ?: "",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
        )
        if (book.authorsToString().isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = book.authorsToString(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
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
            val contentDescription = stringResource(Res.string.no_rated_description)
            Text(
                text = stringResource(Res.string.new_book),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.roseBud,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.semantics {
                    this.contentDescription = contentDescription
                },
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
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.roseBud,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            modifier = Modifier.semantics { hideFromAccessibility() },
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
            .background(MaterialTheme.colorScheme.background)
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
            placeholder = if (MaterialTheme.colorScheme.isLight()) {
                R.drawable.ic_default_book_cover_blue
            } else {
                R.drawable.ic_default_book_cover_white
            },
            contentDescription = book.title,
            contentScale = ContentScale.FillWidth,
            shape = MaterialTheme.shapes.small,
        )
    }
}

@Composable
private fun BookBasicInfo(title: String, subtitle: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.displaySmall,
        color = MaterialTheme.colorScheme.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
    )
    if (subtitle.isNotBlank()) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.tertiary,
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
                        contentDescription = stringResource(
                            Res.string.increase_priority_description,
                            book.title ?: "",
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (isSwitchRightIconEnabled) {
                IconButton(onClick = onSwitchToRight) {
                    Icon(
                        painter = painterResource(R.drawable.ic_round_switch_right),
                        contentDescription = stringResource(
                            Res.string.decrease_priority_description,
                            book.title ?: "",
                        ),
                        tint = MaterialTheme.colorScheme.primary,
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
                placeholder = if (MaterialTheme.colorScheme.isLight()) {
                    R.drawable.ic_default_book_cover_blue
                } else {
                    R.drawable.ic_default_book_cover_white
                },
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth(),
                contentDescription = book.title,
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
    threshold: Float,
    onSwipe: () -> Unit,
    background: @Composable RowScope.() -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val swipeState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * threshold },
    )
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = direction == SwipeDirection.RIGHT,
        enableDismissFromEndToStart = direction == SwipeDirection.LEFT,
        backgroundContent = background,
        onDismiss = {
            onSwipe()
            coroutineScope.launch {
                swipeState.reset()
            }
        },
        content = content,
    )
}

@Composable
fun SwipeItemBackground(
    direction: SwipeDirection,
    color: Color,
    accessibilityPainter: AccessibilityPainter? = null,
) {
    Row(modifier = Modifier.fillMaxSize()) {
        val alignment = when (direction) {
            SwipeDirection.RIGHT -> Alignment.CenterStart
            SwipeDirection.LEFT -> Alignment.CenterEnd
        }
        if (direction == SwipeDirection.LEFT) {
            Spacer(modifier = Modifier.fillMaxWidth(.1f))
        }
        Box(
            modifier = Modifier
                .background(color)
                .fillMaxHeight()
                .run {
                    if (direction == SwipeDirection.RIGHT) {
                        fillMaxWidth(0.9f)
                    } else {
                        fillMaxWidth()
                    }
                },
            contentAlignment = alignment,
        ) {
            if (accessibilityPainter != null) {
                Icon(
                    accessibilityPainter.painter,
                    contentDescription = accessibilityPainter.contentDescription,
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
}

@CustomPreviewLightDarkWithBackground
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

@CustomPreviewLightDarkWithBackground
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

@CustomPreviewLightDark
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

@CustomPreviewLightDarkWithBackground
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

@CustomPreviewLightDarkWithBackground
@Composable
private fun SwipeItemToLeftPreview() {
    ReaderCollectionTheme {
        SwipeItem(
            direction = SwipeDirection.LEFT,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    direction = SwipeDirection.LEFT,
                    color = MaterialTheme.colorScheme.roseBud,
                    accessibilityPainter = painterResource(R.drawable.ic_save_book)
                        .withDescription(null),
                )
            },
            content = {
                Box(Modifier.size(200.dp))
            },
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun SwipeItemToRightPreview() {
    ReaderCollectionTheme {
        SwipeItem(
            direction = SwipeDirection.RIGHT,
            threshold = 0.6f,
            onSwipe = {},
            background = {
                SwipeItemBackground(
                    direction = SwipeDirection.RIGHT,
                    color = MaterialTheme.colorScheme.roseBud,
                    accessibilityPainter = painterResource(R.drawable.ic_remove_book)
                        .withDescription(null),
                )
            },
            content = {
                Box(Modifier.size(200.dp))
            },
        )
    }
}