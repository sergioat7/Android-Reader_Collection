/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 29/8/2024
 */

package aragones.sergio.readercollection.presentation.theme

import android.graphics.Color as AndroidColor
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EbonyClay,
    secondary = White,
    tertiary = LightEbonyClay,
    background = White,
    surface = White,
    error = Color.Red,
)

private val DarkColorScheme = darkColorScheme(
    primary = White,
    secondary = EbonyClay,
    tertiary = LightWhite,
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
    val systemBarAsBackground = if (darkTheme) {
        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    } else {
        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
    }
    val systemBarOppositeToBackground = if (darkTheme) {
        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
    } else {
        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
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

    val activity = LocalActivity.current as ComponentActivity
    SideEffect {
        activity.enableEdgeToEdge(
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
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content,
    )
}