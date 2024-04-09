/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R

@Preview
@Composable
fun CustomToolbarPreview() {

    CustomToolbar(
        title = "Toolbar",
        onBack = {},
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort_books),
                    contentDescription = ""
                )
            }
        })
}

@Composable
fun CustomToolbar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = TextStyle(
                    color = colorResource(id = R.color.textPrimary),
                    fontFamily = robotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(id = R.dimen.text_size_24sp).value.sp
                )
            )
        },
        modifier = modifier,
        backgroundColor = colorResource(id = R.color.colorSecondary),
        elevation = 0.dp,
        navigationIcon = onBack?.let {
            {
                IconButton(onClick = { it.invoke() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                        contentDescription = ""
                    )
                }
            }
        },
        actions = actions
    )
}