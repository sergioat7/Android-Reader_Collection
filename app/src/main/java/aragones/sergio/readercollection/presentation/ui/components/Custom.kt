/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/5/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.lightRoseBud
import aragones.sergio.readercollection.presentation.ui.theme.roseBud

@Composable
fun NoResultsComponent(text: String = stringResource(id = R.string.no_results_text)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.image_no_results),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f),
        )
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp),
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    isSelectable: Boolean = false,
) {

    val starSize = with(LocalDensity.current) { (12f * density).dp }
    val starSpacing = with(LocalDensity.current) { (0.5f * density).dp }

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxStars) {
            val (icon, tint) = when {
                i <= rating -> Pair(R.drawable.ic_round_star_24, MaterialTheme.colors.roseBud)
                i.toFloat() == rating + 0.5f -> Pair(
                    R.drawable.ic_round_star_half_24,
                    MaterialTheme.colors.roseBud
                )

                else -> Pair(R.drawable.ic_round_star_border_24, MaterialTheme.colors.lightRoseBud)
            }
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .selectable(
                        selected = isSelectable,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        },
                    )
                    .size(starSize),
            )
            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoResultsComponentPreview() {
    ReaderCollectionTheme {
        NoResultsComponent()
    }
}

@Preview(showBackground = true)
@Composable
private fun StartRatingBarPreview() {
    ReaderCollectionTheme {
        StarRatingBar(
            rating = 7f / 2,
            onRatingChanged = {},
        )
    }
}