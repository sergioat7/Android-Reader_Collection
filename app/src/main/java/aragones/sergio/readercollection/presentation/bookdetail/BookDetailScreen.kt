/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/3/2025
 */

package aragones.sergio.readercollection.presentation.bookdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.LocalLanguage
import aragones.sergio.readercollection.presentation.components.CustomChip
import aragones.sergio.readercollection.presentation.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.DateCustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.ImageWithLoading
import aragones.sergio.readercollection.presentation.components.MultilineCustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.StarRatingBar
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import aragones.sergio.readercollection.presentation.theme.isLight
import aragones.sergio.readercollection.utils.Constants.FORMATS
import aragones.sergio.readercollection.utils.Constants.STATES
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.CustomInputType
import com.aragones.sergio.util.extensions.isNotBlank
import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import java.util.Calendar
import java.util.Date

@Composable
fun BookDetailScreen(
    state: BookDetailUiState,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onCancel: () -> Unit,
    onSave: (Book) -> Unit,
    onChangeData: (Book) -> Unit,
    onSetImage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
    ) {
        if (state.book == null) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        } else {
            val actions: @Composable RowScope.() -> Unit = when {
                state.isAlreadySaved && !state.isEditable -> {
                    {
                        TopAppBarIcon(
                            icon = R.drawable.ic_edit_book,
                            onClick = onEdit,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        TopAppBarIcon(
                            icon = R.drawable.ic_remove_book,
                            onClick = onRemove,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                state.isAlreadySaved && state.isEditable -> {
                    {
                        TopAppBarIcon(
                            icon = R.drawable.ic_cancel_changes,
                            onClick = onCancel,
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                        TopAppBarIcon(
                            icon = R.drawable.ic_save_changes,
                            onClick = {
                                onSave(state.book)
                            },
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
                else -> {
                    {
                        TopAppBarIcon(
                            icon = R.drawable.ic_save_book,
                            onClick = {
                                onSave(state.book)
                            },
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
            val scrollState = rememberScrollState()
            CustomToolbar(
                title = "",
                modifier = Modifier,
                backgroundColor = MaterialTheme.colorScheme.primary,
                backTintColor = MaterialTheme.colorScheme.secondary,
                onBack = onBack,
                actions = actions,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
            ) {
                Box(
                    modifier = Modifier
                        .height(400.dp)
                        .padding(bottom = 24.dp),
                ) {
                    ImageWithLoading(
                        imageUrl = state.book.thumbnail ?: state.book.image,
                        placeholder = if (MaterialTheme.colorScheme.isLight()) {
                            R.drawable.ic_default_book_cover_white
                        } else {
                            R.drawable.ic_default_book_cover_blue
                        },
                        shape = MaterialTheme.shapes.medium,
                        contentScale = ContentScale.Fit,
                    )
                    if (state.isEditable) {
                        FloatingActionButton(
                            onClick = onSetImage,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(vertical = 8.dp)
                                .offset(0.dp, 24.dp),
                            contentColor = MaterialTheme.colorScheme.secondary,
                            containerColor = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_a_photo),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                BookDetailContent(
                    book = state.book,
                    isEditable = state.isEditable,
                    onChangeData = onChangeData,
                    modifier = Modifier
                        .fillMaxWidth()
//                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))// Due to an unknown error, if I set this, multiline text fields stop working correctly
                        .background(MaterialTheme.colorScheme.background)
                        .padding(
                            start = 12.dp,
                            top = 24.dp,
                            end = 12.dp,
                            bottom = 0.dp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun BookDetailContent(
    book: Book,
    isEditable: Boolean,
    onChangeData: (Book) -> Unit,
    modifier: Modifier = Modifier,
) {
    val language = LocalLanguage.current
    Column(modifier = modifier) {
        HorizontalDivider(
            modifier = Modifier
                .width(100.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally),
            thickness = 5.dp,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(24.dp))
        StarRatingBar(
            rating = book.rating.toFloat(),
            onRatingChanged = {
                if (isEditable) {
                    onChangeData(book.copy(rating = it.toDouble()))
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            maxStars = 10,
            isSelectable = isEditable,
        )
        Spacer(Modifier.height(12.dp))
        CustomOutlinedTextField(
            text = book.title?.takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(title = it))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholderText = stringResource(R.string.add_title),
            textStyle = MaterialTheme.typography.displayLarge,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.title?.isNotBlank() == true
            },
            inputType = CustomInputType.MULTI_LINE_TEXT,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(title = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = book.authorsToString().takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(authors = it.split(",")))
            },
            modifier = Modifier.fillMaxWidth(),
            placeholderText = stringResource(R.string.add_author),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.authorsToString().isNotBlank()
            },
            inputType = CustomInputType.MULTI_LINE_TEXT,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(authors = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        book.categories?.takeIf { it.isNotEmpty() }?.let { categories ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                categories.forEach {
                    CustomChip(it)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        MultilineCustomOutlinedTextField(
            text = book.description.takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(description = it))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.description),
            placeholderText = stringResource(R.string.add_description),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.description?.isNotBlank() == true
            },
            maxLength = 10240,
            maxLines = 8,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(description = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        MultilineCustomOutlinedTextField(
            text = book.summary.takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(summary = it))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.summary),
            placeholderText = stringResource(R.string.add_summary),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.summary?.isNotBlank() == true
            },
            maxLength = 10240,
            maxLines = 8,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(summary = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        val stateValues = STATES.map { it.name }
        val bookState =
            if (book.state == null) {
                stateValues.first()
            } else {
                stateValues[STATES.map { it.id }.indexOf(book.state)]
            }
        CustomDropdownMenu(
            currentValue = bookState,
            values = stateValues,
            onOptionSelected = { newStateValue ->
                val newStateId = STATES.firstOrNull { it.name == newStateValue }?.id
                val newReadingDate = Date().takeIf {
                    book.readingDate == null && newStateId == BookState.READ
                } ?: book.readingDate
                onChangeData(
                    book.copy(
                        state = newStateId,
                        readingDate = newReadingDate,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.state),
            placeholderText = stringResource(R.string.select_state),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            enabled = isEditable,
        )
        Spacer(Modifier.height(8.dp))
        DateCustomOutlinedTextField(
            text = book.readingDate.getValueToShow(language, isEditable),
            onTextChanged = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it
                val dateString = calendar.time.toString(
                    format = Constants.getDateFormatToShow(language),
                    language = language,
                )
                onChangeData(
                    book.copy(
                        readingDate = dateString.toDate(
                            format = Constants.getDateFormatToShow(language),
                            language = language,
                        ),
                        state = BookState.READ,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.reading_date),
            placeholderText = stringResource(R.string.select_a_date),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.readingDate != null
            },
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(readingDate = null))
            }.takeIf { isEditable },
            language = language,
        )
        Spacer(Modifier.height(8.dp))
        val formatValues = FORMATS.map { it.name }
        val bookFormat =
            if (book.format == null) {
                formatValues.first()
            } else {
                formatValues[FORMATS.map { it.id }.indexOf(book.format)]
            }
        CustomDropdownMenu(
            currentValue = bookFormat,
            values = formatValues,
            onOptionSelected = { newFormatValue ->
                val newFormatId = FORMATS.firstOrNull { it.name == newFormatValue }?.id
                onChangeData(book.copy(format = newFormatId))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.format),
            placeholderText = stringResource(R.string.select_format),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            enabled = isEditable,
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = book.pageCount
                .takeIf { it > 0 }
                ?.toString()
                .orElse(isEditable),
            onTextChanged = { newPages ->
                val pages = newPages.takeIf { it.isNotBlank() }?.toInt() ?: 0
                onChangeData(book.copy(pageCount = pages))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.pages),
            placeholderText = stringResource(R.string.add_pages),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.pageCount > 0
            },
            inputType = CustomInputType.NUMBER,
            maxLength = 5,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(pageCount = 0))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = book.isbn.takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(isbn = it))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.isbn),
            placeholderText = stringResource(R.string.add_isbn),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.isbn?.isNotBlank() == true
            },
            inputType = CustomInputType.NUMBER,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(isbn = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = book.publisher.takeIf { it.isNotBlank() }.orElse(isEditable),
            onTextChanged = {
                onChangeData(book.copy(publisher = it))
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.publisher),
            placeholderText = stringResource(R.string.add_publisher),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            textColor = MaterialTheme.colorScheme.description,
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.publisher?.isNotBlank() == true
            },
            inputType = CustomInputType.MULTI_LINE_TEXT,
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(publisher = null))
            }.takeIf { isEditable },
        )
        Spacer(Modifier.height(8.dp))
        DateCustomOutlinedTextField(
            text = book.publishedDate.getValueToShow(language, isEditable),
            onTextChanged = {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = it
                val dateString = calendar.time.toString(
                    format = Constants.getDateFormatToShow(language),
                    language = language,
                )
                onChangeData(
                    book.copy(
                        publishedDate = dateString.toDate(
                            format = Constants.getDateFormatToShow(language),
                            language = language,
                        ),
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            labelText = stringResource(R.string.published_date),
            placeholderText = stringResource(R.string.select_a_date),
            inputHintTextColor = MaterialTheme.colorScheme.description.copy(alpha = 0.75f),
            endIcon = (R.drawable.ic_clear_text).takeIf {
                isEditable && book.publishedDate != null
            },
            enabled = isEditable,
            onEndIconClicked = {
                onChangeData(book.copy(publishedDate = null))
            }.takeIf { isEditable },
            language = language,
        )
    }
}

private fun String?.orElse(isEditable: Boolean): String =
    this ?: Constants.NO_VALUE.takeIf { !isEditable } ?: Constants.EMPTY_VALUE

private fun Date?.getValueToShow(language: String, isEditable: Boolean): String = this?.toString(
    format = Constants.getDateFormatToShow(language),
    language = language,
)
    ?: Constants.NO_VALUE.takeIf { !isEditable }
    ?: Constants.EMPTY_VALUE

@CustomPreviewLightDark
@Composable
fun BookDetailScreenPreview(
    @PreviewParameter(BookDetailScreenPreviewParameterProvider::class) state: BookDetailUiState,
) {
    ReaderCollectionTheme {
        CompositionLocalProvider(LocalLanguage provides "en") {
            BookDetailScreen(
                state = state,
                onBack = {},
                onEdit = {},
                onRemove = {},
                onCancel = {},
                onSave = {},
                onChangeData = {},
                onSetImage = {},
            )
        }
    }
}

private class BookDetailScreenPreviewParameterProvider :
    PreviewParameterProvider<BookDetailUiState> {

    override val values: Sequence<BookDetailUiState>
        get() = sequenceOf(
            BookDetailUiState(
                book = Book(
                    id = "1",
                    title = "Book title",
                    subtitle = "Book subtitle",
                    authors = listOf("Author1", "Author 2 with a long name"),
                    publisher = "Publisher",
                    publishedDate = null,
                    readingDate = null,
                    description =
                    """
                    "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
                    incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
                    exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute
                    irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
                    pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia
                    deserunt mollit anim id est laborum."
                    """.trimIndent(),
                    summary = "Summary",
                    isbn = null,
                    pageCount = 100,
                    categories = listOf(
                        "Category 1",
                        "Category 2",
                        "Category 3",
                        "Category 4",
                        "Category 5",
                        "Category 6",
                    ),
                    averageRating = 7.0,
                    ratingsCount = 100,
                    rating = 5.0,
                    thumbnail = null,
                    image = null,
                    format = "PHYSICAL",
                    state = BookState.READING,
                    priority = 0,
                ),
                isAlreadySaved = true,
                isEditable = false,
            ),
            BookDetailUiState(
                book = Book(
                    id = "1",
                    title = "Book title",
                    subtitle = "Book subtitle",
                    authors = listOf("Author1", "Author 2 with a long name"),
                    publisher = "Publisher",
                    publishedDate = null,
                    readingDate = null,
                    description = "Description",
                    summary = "Summary",
                    isbn = null,
                    pageCount = 100,
                    categories = listOf("Category"),
                    averageRating = 7.0,
                    ratingsCount = 100,
                    rating = 5.0,
                    thumbnail = null,
                    image = null,
                    format = "PHYSICAL",
                    state = BookState.READING,
                    priority = 0,
                ),
                isAlreadySaved = true,
                isEditable = true,
            ),
            BookDetailUiState(
                book = Book(
                    id = "1",
                    title = "Book title",
                    subtitle = "Book subtitle",
                    authors = listOf("Author1", "Author 2 with a long name"),
                    publisher = "Publisher",
                    publishedDate = null,
                    readingDate = null,
                    description = null,
                    summary = null,
                    isbn = null,
                    pageCount = 100,
                    categories = null,
                    averageRating = 7.0,
                    ratingsCount = 100,
                    rating = 5.0,
                    thumbnail = null,
                    image = null,
                    format = "PHYSICAL",
                    state = BookState.READING,
                    priority = 0,
                ),
                isAlreadySaved = false,
                isEditable = false,
            ),
            BookDetailUiState(
                book = null,
                isAlreadySaved = false,
                isEditable = false,
            ),
        )
}