/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/5/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import coil.compose.AsyncImage

@Composable
fun ImageWithLoading(
    imageUrl: String?,
    @DrawableRes placeholder: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    shape: CornerBasedShape? = null,
) {
    var isLoading by rememberSaveable { mutableStateOf(true) }

    Box(modifier) {
        AsyncImage(
            model = imageUrl?.replace("http:", "https:"),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .run {
                    if (shape != null) clip(shape) else this
                },
            placeholder = painterResource(id = placeholder),
            error = painterResource(id = placeholder),
            onLoading = { isLoading = true },
            onSuccess = { isLoading = false },
            onError = { isLoading = false },
            contentScale = contentScale,
        )
        if (isLoading) {
            CircularProgressIndicator(
                color = MaterialTheme.colors.primary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun ImageWithLoadingPreview() {
    ReaderCollectionTheme {
        ImageWithLoading(
            imageUrl = null,
            placeholder = R.drawable.ic_default_book_cover_blue,
        )
    }
}