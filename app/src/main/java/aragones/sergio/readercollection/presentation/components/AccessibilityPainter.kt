/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/8/2025
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

@Immutable
data class AccessibilityPainter(
    val painter: Painter,
    val contentDescription: String?,
)

fun Painter.withDescription(contentDescription: String?) =
    AccessibilityPainter(this, contentDescription)
