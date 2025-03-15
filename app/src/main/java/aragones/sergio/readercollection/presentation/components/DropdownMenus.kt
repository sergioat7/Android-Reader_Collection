/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import kotlin.math.max
import kotlin.math.min

@Composable
fun CustomDropdownMenu(
    currentValue: String,
    modifier: Modifier,
    labelText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    textColor: Color = MaterialTheme.colors.primary,
    values: List<String>,
    onOptionSelected: (String) -> Unit,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    val label: @Composable (() -> Unit)? = labelText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.description,
            )
        }
    }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                color = inputHintTextColor,
            )
        }
    }
    val trailingIcon: @Composable (() -> Unit) =
        {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = textColor,
                )
            }
        }

    Column(modifier) {
        OutlinedTextField(
            value = currentValue,
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (enabled) {
                        it.clickable { expanded = true }
                    } else {
                        it
                    }
                },
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                disabledBorderColor = MaterialTheme.colors.primaryVariant.takeIf { enabled }
                    ?: Color.Transparent,
            ),
            textStyle = MaterialTheme.typography.body1.copy(color = textColor),
            label = label,
            placeholder = placeholder,
            trailingIcon = trailingIcon.takeIf { enabled },
            singleLine = true,
            enabled = false,
            readOnly = true,
            onValueChange = onOptionSelected,
        )
        MyDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.background),
        ) {
            for (value in values) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onOptionSelected(value)
                    },
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.body1,
                        color = textColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun MyDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    scrollState: ScrollState = rememberScrollState(),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    val expandedStates = remember { MutableTransitionState(false) }
    expandedStates.targetState = expanded

    if (expandedStates.currentState || expandedStates.targetState) {
        val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
        val density = LocalDensity.current
        val popupPositionProvider = DropdownMenuPositionProvider(
            offset,
            density,
        ) { parentBounds, menuBounds ->
            transformOriginState.value = calculateTransformOrigin(parentBounds, menuBounds)
        }

        Popup(
            onDismissRequest = onDismissRequest,
            popupPositionProvider = popupPositionProvider,
            properties = properties,
        ) {
            MyDropdownMenuContent(
                expandedStates = expandedStates,
                transformOriginState = transformOriginState,
                scrollState = scrollState,
                modifier = modifier,
                content = content,
            )
        }
    }
}

// Menu open/close animation.
private const val InTransitionDuration = 120
private const val OutTransitionDuration = 75

// Size defaults.
private val MenuElevation = 8.dp
private val MenuVerticalMargin = 48.dp

@Composable
private fun MyDropdownMenuContent(
    expandedStates: MutableTransitionState<Boolean>,
    transformOriginState: MutableState<TransformOrigin>,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    // Menu open/close animation.
    val transition = updateTransition(expandedStates, "DropDownMenu")

    val scale by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(
                    durationMillis = InTransitionDuration,
                    easing = LinearOutSlowInEasing,
                )
            } else {
                // Expanded to dismissed.
                tween(
                    durationMillis = 1,
                    delayMillis = OutTransitionDuration - 1,
                )
            }
        },
        label = "",
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0.8f
        }
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            if (false isTransitioningTo true) {
                // Dismissed to expanded
                tween(durationMillis = 30)
            } else {
                // Expanded to dismissed.
                tween(durationMillis = OutTransitionDuration)
            }
        },
        label = "",
    ) {
        if (it) {
            // Menu is expanded.
            1f
        } else {
            // Menu is dismissed.
            0f
        }
    }
    Card(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                transformOrigin = transformOriginState.value
            },
        elevation = MenuElevation,
        backgroundColor = MaterialTheme.colors.background,
    ) {
        Column(
            modifier = modifier
                .width(IntrinsicSize.Max)
                .verticalScroll(scrollState),
            content = content,
        )
    }
}

// Menu positioning.

/**
 * Calculates the position of a Material [DropdownMenu].
 */
@Immutable
private data class DropdownMenuPositionProvider(
    val contentOffset: DpOffset,
    val density: Density,
    val onPositionCalculated: (IntRect, IntRect) -> Unit = { _, _ -> },
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        // The min margin above and below the menu, relative to the screen.
        val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }
        // The content offset specified using the dropdown offset parameter.
        val contentOffsetX = with(density) {
            contentOffset.x.roundToPx() * (if (layoutDirection == LayoutDirection.Ltr) 1 else -1)
        }
        val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

        // Compute horizontal position.
        val leftToAnchorLeft = anchorBounds.left + contentOffsetX
        val rightToAnchorRight = anchorBounds.right - popupContentSize.width + contentOffsetX
        val rightToWindowRight = windowSize.width - popupContentSize.width
        val leftToWindowLeft = 0
        val x = if (layoutDirection == LayoutDirection.Ltr) {
            sequenceOf(
                leftToAnchorLeft,
                rightToAnchorRight,
                // If the anchor gets outside of the window on the left, we want to position
                // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
                if (anchorBounds.left >= 0) rightToWindowRight else leftToWindowLeft,
            )
        } else {
            sequenceOf(
                rightToAnchorRight,
                leftToAnchorLeft,
                // If the anchor gets outside of the window on the right, we want to position
                // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
                if (anchorBounds.right <= windowSize.width) {
                    leftToWindowLeft
                } else {
                    rightToWindowRight
                },
            )
        }.firstOrNull {
            it >= 0 && it + popupContentSize.width <= windowSize.width
        } ?: rightToAnchorRight

        // Compute vertical position.
        val topToAnchorBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
        val bottomToAnchorTop = anchorBounds.top - popupContentSize.height + contentOffsetY
        val centerToAnchorTop = anchorBounds.top - popupContentSize.height / 2 + contentOffsetY
        val bottomToWindowBottom = windowSize.height - popupContentSize.height - verticalMargin
        val y = sequenceOf(
            topToAnchorBottom,
            bottomToAnchorTop,
            centerToAnchorTop,
            bottomToWindowBottom,
        ).firstOrNull {
            it >= verticalMargin &&
                it + popupContentSize.height <= windowSize.height - verticalMargin
        } ?: bottomToAnchorTop

        onPositionCalculated(
            anchorBounds,
            IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height),
        )
        return IntOffset(x, y)
    }
}

private fun calculateTransformOrigin(parentBounds: IntRect, menuBounds: IntRect): TransformOrigin {
    val pivotX = when {
        menuBounds.left >= parentBounds.right -> {
            0f
        }
        menuBounds.right <= parentBounds.left -> {
            1f
        }
        menuBounds.width == 0 -> {
            0f
        }
        else -> {
            val intersectionCenter =
                (
                    max(parentBounds.left, menuBounds.left) +
                        min(parentBounds.right, menuBounds.right)
                    ) / 2
            (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
        }
    }
    val pivotY = when {
        menuBounds.top >= parentBounds.bottom -> {
            0f
        }
        menuBounds.bottom <= parentBounds.top -> {
            1f
        }
        menuBounds.height == 0 -> {
            0f
        }
        else -> {
            val intersectionCenter =
                (
                    max(parentBounds.top, menuBounds.top) +
                        min(parentBounds.bottom, menuBounds.bottom)
                    ) / 2
            (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
        }
    }
    return TransformOrigin(pivotX, pivotY)
}

@PreviewLightDarkWithBackground
@Composable
private fun CustomDropdownMenuPreview() {
    ReaderCollectionTheme {
        CustomDropdownMenu(
            currentValue = "Value",
            labelText = "Header",
            placeholderText = "Please choose",
            modifier = Modifier.padding(12.dp),
            values = listOf("Option 1", "Option 2", "Option 3"),
            onOptionSelected = {},
        )
    }
}