/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2026
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb

object AppUiProvider {

    @Composable
    fun isDarkThemeApplied(): Boolean {
        val darkTheme = when (AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.MODE_NIGHT_YES -> true
            AppCompatDelegate.MODE_NIGHT_NO -> false
            else -> isSystemInDarkTheme()
        }
        return darkTheme
    }

    @Composable
    fun applyBarsStyle(
        isDarkTheme: Boolean,
        colors: ColorScheme,
        statusBarSameAsBackground: Boolean,
        navigationBarSameAsBackground: Boolean,
    ) {
        val systemBarAsBackground = if (isDarkTheme) {
            SystemBarStyle.dark(
                colors.secondary.toArgb(),
            )
        } else {
            SystemBarStyle.light(
                colors.secondary.toArgb(),
                colors.secondary.toArgb(),
            )
        }
        val systemBarOppositeToBackground = if (isDarkTheme) {
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

        val activity = LocalActivity.current as ComponentActivity
        SideEffect {
            activity.enableEdgeToEdge(
                statusBarStyle = statusBarStyle,
                navigationBarStyle = navigationBarStyle,
            )
        }
    }
}