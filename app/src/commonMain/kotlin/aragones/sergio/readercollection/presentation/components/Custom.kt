/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/5/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.lightRoseBud
import aragones.sergio.readercollection.presentation.theme.roseBud
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.book_rating_description
import reader_collection.app.generated.resources.clear_text
import reader_collection.app.generated.resources.ic_clear_text
import reader_collection.app.generated.resources.ic_round_star_24
import reader_collection.app.generated.resources.ic_round_star_border_24
import reader_collection.app.generated.resources.ic_round_star_half_24
import reader_collection.app.generated.resources.ic_search
import reader_collection.app.generated.resources.image_no_results
import reader_collection.app.generated.resources.no_results_text
import reader_collection.app.generated.resources.search
import reader_collection.app.generated.resources.star_empty
import reader_collection.app.generated.resources.star_filled
import reader_collection.app.generated.resources.star_half_filled
import reader_collection.app.generated.resources.star_rate_description
import reader_collection.app.generated.resources.star_status_description

@Composable
fun NoResultsComponent(
    modifier: Modifier = Modifier,
    text: String = stringResource(Res.string.no_results_text),
    image: DrawableResource = Res.drawable.image_no_results,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(image),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    isSelectable: Boolean = false,
) {
    val starSize = with(LocalDensity.current) { (12f * density).dp }
    val starSpacing = with(LocalDensity.current) { (0.5f * density).dp }

    val contentDescription =
        stringResource(Res.string.book_rating_description, (rating * 10 / maxStars).toInt())
    Row(
        modifier = modifier
            .selectableGroup()
            .semantics {
                this.contentDescription = contentDescription
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxStars) {
            val (icon, tint) = when {
                i <= rating -> Pair(
                    Res.drawable.ic_round_star_24,
                    MaterialTheme.colorScheme.roseBud,
                )
                i.toFloat() == rating + 0.5f -> Pair(
                    Res.drawable.ic_round_star_half_24,
                    MaterialTheme.colorScheme.roseBud,
                )
                else -> Pair(
                    Res.drawable.ic_round_star_border_24,
                    MaterialTheme.colorScheme.lightRoseBud,
                )
            }
            val stateText = stringResource(
                when {
                    i <= rating -> Res.string.star_filled
                    i.toFloat() == rating + 0.5f -> Res.string.star_half_filled
                    else -> Res.string.star_empty
                },
            )
            val statusDescription =
                stringResource(Res.string.star_status_description, stateText, i, maxStars)
            val starContentDescription =
                pluralStringResource(Res.plurals.star_rate_description, i, statusDescription, i)
            IconButton(
                onClick = { onRatingChanged(i.toFloat()) },
                modifier = Modifier
                    .then(
                        if (!isSelectable) {
                            Modifier.semantics { hideFromAccessibility() }
                        } else {
                            Modifier
                        },
                    ).size(starSize),
                enabled = isSelectable,
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = starContentDescription.takeIf { isSelectable },
                    tint = tint,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            if (i < maxStars) {
                Spacer(modifier = Modifier.width(starSpacing))
            }
        }
    }
}

@Composable
fun SearchBar(
    text: String,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    showLeadingIcon: Boolean = false,
    inputHintTextColor: Color = MaterialTheme.colorScheme.tertiary,
    textColor: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    requestFocusByDefault: Boolean = true,
) {
    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = when {
                    text.isEmpty() -> TextRange.Zero
                    else -> TextRange(text.length, text.length)
                },
            ),
        )
    }

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        if (requestFocusByDefault) focusManager.moveFocus(FocusDirection.Down)
    }

    val placeholder: @Composable (() -> Unit) = {
        Text(
            text = stringResource(Res.string.search),
            color = inputHintTextColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    val leadingIcon: @Composable (() -> Unit)? = if (showLeadingIcon) {
        {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = stringResource(Res.string.search),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        null
    }
    val trailingIcon: @Composable (() -> Unit)? = if (textFieldValueState.text.isNotBlank()) {
        {
            TopAppBarIcon(
                accessibilityPainter = painterResource(Res.drawable.ic_clear_text)
                    .withDescription(stringResource(Res.string.clear_text)),
                onClick = {
                    textFieldValueState = TextFieldValue("")
                },
            )
        }
    } else {
        null
    }

    OutlinedTextField(
        value = textFieldValueState,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
        ),
        textStyle = textStyle.copy(color = textColor),
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboard?.hide()
                focusManager.clearFocus()
                onSearch(textFieldValueState.text)
            },
        ),
        singleLine = true,
        onValueChange = {
            textFieldValueState = it
        },
    )
}

@Composable
fun CustomFilterChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    border: BorderStroke? = null,
    selectedIcon: AccessibilityPainter? = null,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                ),
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        },
        modifier = modifier,
        leadingIcon = selectedIcon?.let {
            {
                Icon(
                    painter = it.painter,
                    contentDescription = it.contentDescription,
                )
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            labelColor = MaterialTheme.colorScheme.primary,
            iconColor = MaterialTheme.colorScheme.primary,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.secondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.secondary,
            selectedTrailingIconColor = MaterialTheme.colorScheme.secondary,
        ),
        border = border?.takeIf { !selected },
    )
}

@Composable
fun CustomChip(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(8.dp),
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun NoResultsComponentPreview() {
    ReaderCollectionTheme {
        NoResultsComponent()
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun StartRatingBarPreview() {
    ReaderCollectionTheme {
        StarRatingBar(
            rating = 7f / 2,
            onRatingChanged = {},
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun SearchBarPreview() {
    ReaderCollectionTheme {
        SearchBar(text = "text", onSearch = {}, showLeadingIcon = true)
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun FilterChipPreview() {
    ReaderCollectionTheme {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CustomFilterChip(
                title = "Value 1",
                selected = true,
                onClick = {},
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                selectedIcon = rememberVectorPainter(Icons.Default.Done)
                    .withDescription(null),
            )
            CustomFilterChip(
                title = "Value 2",
                selected = false,
                onClick = {},
            )
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun CustomChipPreview() {
    ReaderCollectionTheme {
        CustomChip("Value")
    }
}

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Preview(name = "Light-en", group = "Light", locale = "en")
@Preview(name = "Light-es", group = "Light", locale = "es")
@Preview(
    name = "Dark-en",
    group = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    locale = "en",
)
@Preview(
    name = "Dark-es",
    group = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    locale = "es",
)
annotation class CustomPreviewLightDark

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
@Preview(name = "Light-en", group = "Light", locale = "en", showBackground = true)
@Preview(name = "Light-es", group = "Light", locale = "es", showBackground = true)
@Preview(
    name = "Dark-en",
    group = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    locale = "en",
    showBackground = true,
)
@Preview(
    name = "Dark-es",
    group = "Dark",
    uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL,
    locale = "es",
    showBackground = true,
)
annotation class CustomPreviewLightDarkWithBackground

@Composable
internal fun LaunchedEffectOnce(block: () -> Unit) {
    var isLaunched by rememberSaveable { mutableStateOf(false) }
    if (!isLaunched) {
        LaunchedEffect(block) {
            block()
            isLaunched = true
        }
    }
}