/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

fun slideIntoContainer(): EnterTransition = slideInHorizontally(
    animationSpec = tween(
        durationMillis = 200,
        easing = LinearEasing,
    ),
    initialOffsetX = { it },
)

fun slideOutOfContainer(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(
        200,
        easing = LinearEasing,
    ),
    targetOffsetX = { it },
)