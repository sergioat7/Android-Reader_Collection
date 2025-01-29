/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/8/2024
 */

package aragones.sergio.readercollection.presentation.ui.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

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
fun ReaderCollectionApp(
    statusBarSameAsBackground: Boolean = true,
    navigationBarSameAsBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isSystemInDarkTheme()
    }
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    val systemBarAsBackground = if (darkTheme) {
        SystemBarStyle.dark(
            colors.secondary.toArgb(),
        )
    } else {
        SystemBarStyle.light(
            colors.secondary.toArgb(),
            colors.secondary.toArgb(),
        )
    }
    val systemBarOppositeToBackground = if (darkTheme) {
        SystemBarStyle.light(
            colors.primary.toArgb(),
            colors.primary.toArgb(),
        )
    } else {
        SystemBarStyle.dark(colors.primary.toArgb())
    }
    val statusBarStyle = if (statusBarSameAsBackground) {
        systemBarAsBackground
    } else {
        systemBarOppositeToBackground
    }
    val navigationBarStyle = if (navigationBarSameAsBackground) {
        systemBarAsBackground
    } else {
        systemBarOppositeToBackground
    }

    val context = LocalContext.current as ComponentActivity
    SideEffect {
        context.enableEdgeToEdge(
            statusBarStyle = statusBarStyle,
            navigationBarStyle = navigationBarStyle,
        )
    }

    ReaderCollectionTheme(
        darkTheme = darkTheme,
        content = content,
    )
}

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