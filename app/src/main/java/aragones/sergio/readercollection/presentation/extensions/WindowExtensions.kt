/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 10/1/2022
 */

package aragones.sergio.readercollection.presentation.extensions

import android.view.Window
import androidx.core.view.WindowInsetsControllerCompat

fun Window.setStatusBarStyle(color: Int, lightStatusBar: Boolean) {
    statusBarColor = color
    WindowInsetsControllerCompat(this, decorView).isAppearanceLightStatusBars = lightStatusBar
}