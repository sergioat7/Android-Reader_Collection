/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 28/1/2025
 */

package aragones.sergio.readercollection.presentation.landing

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.PreviewLightDarkWithBackground
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.roseBud
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.airbnb.lottie.compose.rememberLottieDynamicProperties
import com.airbnb.lottie.compose.rememberLottieDynamicProperty

@Composable
fun LandingScreen(onAnimationFinished: () -> Unit) {
    val composition = rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.landing_animation),
    )
    val progress = animateLottieCompositionAsState(
        composition = composition.value,
        iterations = 1,
    )
    val dynamicProperties = rememberLottieDynamicProperties(
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.secondary.toArgb(),
            keyPath = arrayOf(
                "01",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.secondary.toArgb(),
            keyPath = arrayOf(
                "02",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.secondary.toArgb(),
            keyPath = arrayOf(
                "03",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.secondary.toArgb(),
            keyPath = arrayOf(
                "04",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.roseBud.toArgb(),
            keyPath = arrayOf(
                "obj_02",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.COLOR,
            value = MaterialTheme.colors.primary.toArgb(),
            keyPath = arrayOf(
                "ombra",
                "**",
            ),
        ),
        rememberLottieDynamicProperty(
            property = LottieProperty.STROKE_COLOR,
            value = MaterialTheme.colors.primary.toArgb(),
            keyPath = arrayOf(
                "**",
            ),
        ),
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LottieAnimation(
            composition = composition.value,
            progress = { progress.value },
            modifier = Modifier.align(Alignment.Center),
            dynamicProperties = dynamicProperties,
        )
    }

    LaunchedEffect(progress.value) {
        if (progress.value == 1f) onAnimationFinished()
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun LandingScreenPreview() {
    ReaderCollectionTheme {
        LandingScreen(onAnimationFinished = {})
    }
}