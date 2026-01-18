/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2026
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

expect object AppUiProvider {
    @Composable
    fun isDarkThemeApplied(): Boolean

    @Composable
    fun applyBarsStyle(
        isDarkTheme: Boolean,
        colors: ColorScheme,
        statusBarSameAsBackground: Boolean,
        navigationBarSameAsBackground: Boolean,
    )

    @Composable
    fun isPortrait(): Boolean

    @Composable
    fun getScreenWidth(): Int

    @Composable
    fun launchWorker()

    @Composable
    fun cancelWorker()
}