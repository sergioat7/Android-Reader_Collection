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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R

@Preview(showBackground = true)
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
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f),
        )
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp),
            style = TextStyle(
                color = colorResource(id = R.color.textPrimary),
                fontFamily = robotoSerifFamily,
                fontWeight = FontWeight.Bold,
                fontSize = dimensionResource(id = R.dimen.text_size_20sp).value.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StartRatingBarPreview() {
    StarRatingBar(
        rating = 7f / 2,
        onRatingChanged = {},
    )
}

@Composable
fun StarRatingBar(
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    rating: Float,
    isSelectable: Boolean = false,
    onRatingChanged: (Float) -> Unit,
) {

    val density = LocalDensity.current.density
    val starSize = (12f * density).dp
    val starSpacing = (0.5f * density).dp

    val colorTertiary = colorResource(id = R.color.colorTertiary)
    val colorTertiaryLight = colorResource(id = R.color.colorTertiaryLight)

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxStars) {
            val (icon, tint) = when {
                i <= rating -> Pair(R.drawable.ic_round_star_24, colorTertiary)
                i.toFloat() == rating + 0.5f -> Pair(
                    R.drawable.ic_round_star_half_24,
                    colorTertiary
                )

                else -> Pair(R.drawable.ic_round_star_border_24, colorTertiaryLight)
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
                    .width(starSize)
                    .height(starSize),
            )

            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}