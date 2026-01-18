/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

actual object AppUiProvider {

    @Composable
    actual fun isDarkThemeApplied(): Boolean {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun applyBarsStyle(
        isDarkTheme: Boolean,
        colors: ColorScheme,
        statusBarSameAsBackground: Boolean,
        navigationBarSameAsBackground: Boolean,
    ) {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun isPortrait(): Boolean {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun getScreenWidth(): Int {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun launchWorker() {
        TODO("Not yet implemented")
    }

    @Composable
    actual fun cancelWorker() {
        TODO("Not yet implemented")
    }
}