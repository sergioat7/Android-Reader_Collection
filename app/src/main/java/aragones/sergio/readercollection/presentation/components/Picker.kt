/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/12/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.selector
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Picker(
    items: List<String>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    currentIndexSelected: Int = 0,
    visibleItemsCount: Int = 3,
) {
    val fadingEdgeGradient = remember {
        Brush.verticalGradient(
            0f to Color.Transparent,
            0.5f to Color.Black,
            1f to Color.Transparent,
        )
    }

    var itemHeightPixels by rememberSaveable { mutableIntStateOf(0) }
    val itemPadding = 16.dp
    val itemHeightDp = itemHeightPixels.pixelsToDp() + itemPadding * 2
    val visibleItemsMiddle = visibleItemsCount / 2

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = currentIndexSelected)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    Box(modifier = modifier.background(MaterialTheme.colors.secondary)) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeightDp),
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp * visibleItemsCount)
                .fadingEdge(fadingEdgeGradient),
        ) {
            itemsIndexed(items) { _, item ->
                Text(
                    text = item,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h3,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .padding(vertical = itemPadding)
                        .onSizeChanged { size ->
                            itemHeightPixels = size.height
                        },
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = itemHeightDp * visibleItemsMiddle)
                .height(itemHeightDp)
                .fillMaxWidth()
                .background(MaterialTheme.colors.selector)
                .zIndex(-1f),
        )
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .collect { onSelect(it) }
    }
}

@Composable
private fun Int.pixelsToDp() = with(LocalDensity.current) { this@pixelsToDp.toDp() }

private fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }

@PreviewLightDark
@Composable
private fun PickerPreview() {
    ReaderCollectionTheme {
        Row(horizontalArrangement = Arrangement.Center) {
            Picker(
                items = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5"),
                onSelect = {},
                modifier = Modifier.weight(1f),
                currentIndexSelected = 0,
            )
            Picker(
                items = listOf("Option 1", "Option 2", "Option 3", "Option 4", "Option 5"),
                onSelect = {},
                modifier = Modifier.weight(1f),
                currentIndexSelected = 3,
            )
        }
    }
}