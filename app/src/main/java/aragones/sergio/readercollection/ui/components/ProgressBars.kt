/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/3/2024
 */

package aragones.sergio.readercollection.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R

@Preview(showBackground = true)
@Composable
fun CustomCircularProgressIndicatorPreview() {
    CustomCircularProgressIndicator()
}

@Composable
fun CustomCircularProgressIndicator() {

    val colorPrimary = colorResource(id = R.color.colorPrimary)

    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(color = colorPrimary, modifier = Modifier.align(Alignment.Center))
    }
}