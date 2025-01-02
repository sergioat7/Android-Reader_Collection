/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/8/2024
 */

package aragones.sergio.readercollection.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColors(
    primary = EbonyClay,
    primaryVariant = LightEbonyClay,
    secondary = White,
    background = White,
    surface = White,
    error = Color.Red,
)

private val DarkColorScheme = darkColors(
    primary = White,
    primaryVariant = LightWhite,
    secondary = EbonyClay,
    background = EbonyClay,
    surface = EbonyClay,
    error = Color.Red,
)

@Composable
fun ReaderCollectionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colors = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}