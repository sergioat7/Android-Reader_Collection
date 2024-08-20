/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.search

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.BookItem
import aragones.sergio.readercollection.presentation.ui.components.CustomSearchBar
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.robotoSerifFamily
import kotlinx.coroutines.launch

@Preview
@Composable
fun SearchScreenPreview() {
    SearchScreen(
        books = listOf(
            Book(
                "1",
                "Harry Potter y la Piedra Filosofal",
                null,
                listOf("J.K. Rowling"),
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
                6.0,
                null,
                null,
                null,
                null,
                false,
                0
            ),
            Book(
                "2",
                "Large title for another searched book in the list",
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
                null,
                false,
                0
            ),
            Book(
                "3",
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
                null,
                false,
                0
            ),
            Book(
                "",
                "",
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
                null,
                false,
                0
            )
        ),
        onSearch = {},
        onBookClick = {},
        onLoadMoreClick = {},
    )
}

@Composable
fun SearchScreen(
    books: List<Book>,
    query: String? = null,
    onSearch: (String) -> Unit,
    onBookClick: (String) -> Unit,
    onLoadMoreClick: () -> Unit,
) {

    val colorSecondary = colorResource(id = R.color.colorSecondary)

    val listState = rememberLazyListState()
    val showTopButton by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }
    val showBottomButton by remember {
        derivedStateOf { !listState.reachedBottom() }
    }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorSecondary)
    ) {
        CustomSearchBar(
            title = stringResource(id = R.string.title_search),
            query = query ?: "",
            modifier = Modifier.background(colorSecondary),
            elevation = if (showTopButton) 4.dp else 0.dp,
            onSearch = {
                onSearch(it)
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            if (books.isEmpty()) {
                NoResultsComponent()
            } else {
                SearchContent(
                    books = books,
                    listState = listState,
                    showTopButton = showTopButton,
                    showBottomButton = showBottomButton,
                    onTopButtonClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = 0)
                        }
                    },
                    onBottomButtonClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(index = listState.layoutInfo.totalItemsCount - 1)
                        }
                    },
                    onBookClick = onBookClick,
                    onLoadMoreClick = onLoadMoreClick,
                )
            }
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
    onLoadMoreClick: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(state = listState) {
            items(items = books, key = { it.id }) {
                if (it.id.isNotBlank()) {
                    BookItem(book = it, onBookClick = onBookClick)
                } else {
                    LoadMoreButton(onLoadMoreClick)
                }
            }
        }

        if (showTopButton) {
            ListButton(
                image = R.drawable.ic_double_arrow_up,
                modifier = Modifier.align(Alignment.TopEnd),
                onClick = onTopButtonClick,
            )
        }

        if (showBottomButton) {
            ListButton(
                image = R.drawable.ic_double_arrow_down,
                modifier = Modifier.align(Alignment.BottomEnd),
                onClick = onBottomButtonClick,
            )
        }
    }
}

@Composable
private fun ListButton(
    @DrawableRes image: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    val colorPrimary = colorResource(id = R.color.colorPrimary)
    val colorSecondary = colorResource(id = R.color.colorSecondary)

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        contentColor = colorSecondary,
        backgroundColor = colorPrimary
    ) {
        Icon(
            painter = painterResource(id = image),
            contentDescription = ""
        )
    }
}

@Composable
private fun LoadMoreButton(onClick: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.align(Alignment.Center),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.colorPrimary),
                disabledBackgroundColor = colorResource(id = R.color.colorPrimaryLight)
            ),
            shape = RoundedCornerShape(15.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add_circle_outline),
                    contentDescription = "",
                    tint = colorResource(id = R.color.textTertiary),
                )
                Text(
                    text = stringResource(id = R.string.load_more),
                    modifier = Modifier.padding(12.dp),
                    style = TextStyle(
                        color = colorResource(id = R.color.textTertiary),
                        fontFamily = robotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
                    ),
                    maxLines = 1,
                )
            }
        }
    }
}

internal fun LazyListState.reachedBottom(buffer: Int = 1): Boolean {
    val lastVisibleItem = this.layoutInfo.visibleItemsInfo.lastOrNull()
    return lastVisibleItem?.index != 0 && lastVisibleItem?.index == this.layoutInfo.totalItemsCount - buffer
}