/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme

@Composable
fun CustomCircularProgressIndicator() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray.copy(alpha = 0.75f))
            .clickable {}//To avoid clicks in views behind
            .pointerInput(Unit) { detectTapGestures(onPress = {}) },//To avoid showing shadow on click
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colors.primary,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun CustomCircularProgressIndicatorPreview() {
    ReaderCollectionTheme {
        CustomCircularProgressIndicator()
    }
}