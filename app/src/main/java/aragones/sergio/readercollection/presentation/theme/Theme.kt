/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/8/2024
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import aragones.sergio.readercollection.presentation.theme.AppUiProvider.applyBarsStyle
import aragones.sergio.readercollection.presentation.theme.AppUiProvider.isDarkThemeApplied

private val LightColorScheme = lightColorScheme(
    primary = EbonyClay,
    secondary = White,
    tertiary = Dolphin,
    background = White,
    surface = White,
    error = Red,
    onError = White,
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = EbonyClay,
    tertiary = Alto,
    background = EbonyClay,
    surface = EbonyClay,
    error = Red,
    onError = White,
)

@Composable
fun ReaderCollectionApp(
    statusBarSameAsBackground: Boolean = true,
    navigationBarSameAsBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val isDarkTheme = isDarkThemeApplied()
    val colors = if (isDarkTheme) DarkColorScheme else LightColorScheme

    applyBarsStyle(
        isDarkTheme = isDarkTheme,
        colors = colors,
        statusBarSameAsBackground = statusBarSameAsBackground,
        navigationBarSameAsBackground = navigationBarSameAsBackground,
    )

    ReaderCollectionTheme(
        isDarkTheme = isDarkTheme,
        content = content,
    )
}

@Composable
fun ReaderCollectionTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (isDarkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}