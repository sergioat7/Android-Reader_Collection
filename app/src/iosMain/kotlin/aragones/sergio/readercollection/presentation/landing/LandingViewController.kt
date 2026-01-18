/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.presentation.landing

import androidx.compose.ui.window.ComposeUIViewController
import aragones.sergio.readercollection.presentation.di.initKoin

fun LandingViewController() = ComposeUIViewController(configure = { initKoin() }) {
}