/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import com.aragones.sergio.util.extensions.isNotBlank

@Composable
fun TopAppBarIcon(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colors.primary,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = tint,
        )
    }
}

@Composable
fun CustomToolbar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    elevation: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.primary,
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.description,
                    )
                }
            }
        },
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = elevation,
        navigationIcon = onBack?.let {
            {
                TopAppBarIcon(
                    icon = R.drawable.ic_arrow_back,
                    onClick = it,
                )
            }
        },
        actions = actions,
    )
}

@Composable
fun CustomSearchBar(
    title: String,
    query: String,
    onSearch: ((String) -> Unit),
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
) {
    var isSearching by rememberSaveable { mutableStateOf(false) }

    val backIcon: @Composable (() -> Unit)? = if (isSearching || onBack != null) {
        {
            TopAppBarIcon(
                icon = R.drawable.ic_arrow_back,
                onClick = {
                    if (isSearching) {
                        isSearching = false
                    } else {
                        onBack?.invoke()
                    }
                },
            )
        }
    } else {
        null
    }

    TopAppBar(
        title = {
            if (isSearching) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SearchBar(
                        text = query,
                        onSearch = {
                            isSearching = false
                            if (it.isNotBlank()) {
                                onSearch(it)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                }
            } else {
                Text(
                    text = query.ifBlank { title },
                    style = MaterialTheme.typography.h1,
                    color = MaterialTheme.colors.primary,
                )
            }
        },
        modifier = modifier,
        backgroundColor = backgroundColor,
        elevation = elevation,
        navigationIcon = backIcon,
        actions = {
            if (!isSearching) {
                TopAppBarIcon(
                    icon = R.drawable.ic_search,
                    onClick = { isSearching = true },
                )
            }
        },
    )
}

@Composable
fun CollapsingToolbar(
    nestedScrollConnection: CollapsingToolbarNestedScrollConnection,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primary,
    elevation: Dp = 0.dp,
    startContent: @Composable RowScope.() -> Unit = {},
    middleContent: @Composable BoxScope.(Modifier) -> Unit = {},
    endContent: @Composable RowScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        contentColor = contentColorFor(backgroundColor),
        elevation = elevation,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppBarDefaults.ContentPadding),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                startContent()
                Spacer(Modifier.weight(1f))
                endContent()
            }
            middleContent(
                Modifier
                    .graphicsLayer {
                        scaleX = nestedScrollConnection.contentScale
                        scaleY = nestedScrollConnection.contentScale
                    }.fillMaxSize()
                    .align(Alignment.TopCenter)
                    .padding(vertical = 24.dp)
                    .zIndex(-1f),
            )
        }
    }
}

class CollapsingToolbarNestedScrollConnection(
    private val maxContentSize: Dp = 300.dp,
    private val minContentSize: Dp = 100.dp,
) : NestedScrollConnection {

    var currentContentSize by mutableStateOf(maxContentSize)
        private set
    var contentScale by mutableFloatStateOf(1f)
        private set
    val isExpanded: Boolean
        get() = currentContentSize > minContentSize

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val delta = available.y
        val newContentSize =
            (currentContentSize + delta.dp).coerceIn(minContentSize, maxContentSize)
        val consumed = newContentSize - currentContentSize

        currentContentSize = newContentSize
        contentScale = currentContentSize / maxContentSize

        return Offset(0f, consumed.value)
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
    ): Offset {
        val delta = available.y
        val newContentSize =
            (currentContentSize + delta.dp).coerceIn(minContentSize, maxContentSize)

        currentContentSize = newContentSize
        contentScale = currentContentSize / maxContentSize

        return Offset.Zero
    }
}

@PreviewLightDark
@Composable
private fun CustomToolbarPreview() {
    ReaderCollectionTheme {
        CustomToolbar(
            title = "Toolbar",
            subtitle = "Subtitle",
            onBack = {},
            actions = {
                TopAppBarIconPreview()
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun CustomSearchBarPreview() {
    ReaderCollectionTheme {
        CustomSearchBar(
            title = stringResource(id = R.string.title_search),
            query = "",
            onSearch = {},
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun TopAppBarIconPreview() {
    ReaderCollectionTheme {
        TopAppBarIcon(
            icon = R.drawable.ic_sort_books,
            onClick = {},
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun CollapsingToolbarPreview() {
    ReaderCollectionTheme {
        CollapsingToolbar(
            nestedScrollConnection = CollapsingToolbarNestedScrollConnection(),
            modifier = Modifier.size(300.dp),
            startContent = {
                TopAppBarIcon(
                    icon = R.drawable.ic_arrow_back,
                    onClick = {},
                    tint = MaterialTheme.colors.secondary,
                )
            },
            middleContent = {
                ImageWithLoading(
                    imageUrl = null,
                    placeholder = if (MaterialTheme.colors.isLight) {
                        R.drawable.ic_default_book_cover_white
                    } else {
                        R.drawable.ic_default_book_cover_blue
                    },
                    modifier = it,
                    shape = MaterialTheme.shapes.medium,
                    contentScale = ContentScale.Fit,
                )
            },
            endContent = {
                TopAppBarIcon(
                    icon = R.drawable.ic_edit_book,
                    onClick = {},
                    tint = MaterialTheme.colors.secondary,
                )
                TopAppBarIcon(
                    icon = R.drawable.ic_remove_book,
                    onClick = {},
                    tint = MaterialTheme.colors.secondary,
                )
            },
        )
    }
}