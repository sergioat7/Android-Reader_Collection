/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIApplication
import platform.UIKit.UIInterfaceOrientationPortrait
import platform.UIKit.UIInterfaceOrientationPortraitUpsideDown
import platform.UIKit.UIScreen

actual object AppUiProvider {

    @Composable
    actual fun isDarkThemeApplied(): Boolean = isSystemInDarkTheme()

    @Composable
    actual fun applyBarsStyle(
        isDarkTheme: Boolean,
        colors: ColorScheme,
        statusBarSameAsBackground: Boolean,
        navigationBarSameAsBackground: Boolean,
    ) {}

    @Composable
    actual fun isPortrait(): Boolean {
        val orientation = UIApplication.sharedApplication.statusBarOrientation
        return orientation == UIInterfaceOrientationPortrait ||
            orientation == UIInterfaceOrientationPortraitUpsideDown
    }

    @OptIn(ExperimentalForeignApi::class)
    @Composable
    actual fun getScreenWidth(): Int =
        UIScreen.mainScreen.bounds.useContents { this.size.width.toInt() }

    @Composable
    actual fun launchWorker() {}

    @Composable
    actual fun cancelWorker() {}
}