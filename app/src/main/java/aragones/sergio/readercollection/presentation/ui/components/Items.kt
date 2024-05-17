/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 17/5/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book

@Preview
@Composable
fun BookItemPreview() {
    BookItem(
        book = Book(
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
            7.0,
            null,
            null,
            null,
            null,
            false,
            0
        ),
        onBookClick = {},
        isDraggingEnabled = true
    )
}

@Composable
fun BookItem(
    book: Book,
    onBookClick: (String) -> Unit,
    isDraggingEnabled: Boolean = false,
) {

    val colorPrimaryLight = colorResource(id = R.color.colorPrimaryLight)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable {
                onBookClick(book.id)
            }
    ) {

        Column {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(top = 24.dp, bottom = 24.dp, end = 24.dp)
            ) {

                if (isDraggingEnabled) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_enable_drag),
                        contentDescription = "",
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .padding(start = 24.dp)
                    )
                }
                ImageWithLoading(
                    imageUrl = book.thumbnail,
                    placeholder = R.drawable.ic_default_book_cover_blue,
                    modifier = Modifier
                        .widthIn(max = 130.dp)
                        .fillMaxHeight()
                        .padding(start = 24.dp),
                    cornerRadius = 10
                )
                BookInfo(book = book)
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 24.dp),
                color = colorPrimaryLight
            )
        }
    }
}

@Composable
fun BookInfo(book: Book) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp)
    ) {

        Text(
            text = book.title ?: "",
            style = TextStyle(
                color = colorResource(id = R.color.textPrimary),
                fontFamily = robotoSerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_size_24sp).value.sp
            ),
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
        )
        if (book.authorsToString().isNotBlank()) {
            Text(
                text = book.authorsToString(),
                modifier = Modifier.padding(top = 8.dp),
                style = TextStyle(
                    color = colorResource(id = R.color.textSecondary),
                    fontFamily = robotoSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (book.rating > 0) {
            Row(modifier = Modifier.height(30.dp), verticalAlignment = Alignment.CenterVertically) {
                StarRatingBar(
                    rating = book.rating.toFloat() / 2,
                    onRatingChanged = {}
                )
                Text(
                    text = book.rating.toInt().toString(),
                    modifier = Modifier.padding(start = 12.dp),
                    style = TextStyle(
                        color = colorResource(id = R.color.textQuaternary),
                        fontFamily = robotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_size_18sp).value.sp
                    ),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.new_book),
                style = TextStyle(
                    color = colorResource(id = R.color.textQuaternary),
                    fontFamily = robotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(id = R.dimen.text_size_24sp).value.sp
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}