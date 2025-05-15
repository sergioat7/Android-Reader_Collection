/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/5/2024
 */

package aragones.sergio.readercollection.presentation.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.lightRoseBud
import aragones.sergio.readercollection.presentation.theme.roseBud

@Composable
fun NoResultsComponent(
    text: String = stringResource(R.string.no_results_text),
    @DrawableRes image: Int = R.drawable.image_no_results,
) {
    Column(
        modifier = Modifier
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

    Row(
        modifier = modifier.selectableGroup(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..maxStars) {
            val (icon, tint) = when {
                i <= rating -> Pair(R.drawable.ic_round_star_24, MaterialTheme.colorScheme.roseBud)
                i.toFloat() == rating + 0.5f -> Pair(
                    R.drawable.ic_round_star_half_24,
                    MaterialTheme.colorScheme.roseBud,
                )
                else -> Pair(
                    R.drawable.ic_round_star_border_24,
                    MaterialTheme.colorScheme.lightRoseBud,
                )
            }
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .selectable(
                        selected = isSelectable,
                        onClick = {
                            onRatingChanged(i.toFloat())
                        },
                    ).size(starSize),
            )
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
            text = stringResource(R.string.search),
            color = inputHintTextColor,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
    val leadingIcon: @Composable (() -> Unit)? = if (showLeadingIcon) {
        {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    } else {
        null
    }
    val trailingIcon: @Composable (() -> Unit)? = if (textFieldValueState.text.isNotBlank()) {
        {
            TopAppBarIcon(
                icon = R.drawable.ic_clear_text,
                onClick = {
                    textFieldValueState = textFieldValueState.copy("")
                    if (!requestFocusByDefault) {
                        keyboard?.hide()
                        focusManager.clearFocus()
                        onSearch("")
                    }
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
    @DrawableRes selectedIcon: Int? = null,
    selectedImage: ImageVector? = null,
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
                    painter = painterResource(it),
                    contentDescription = null,
                )
            }
        } ?: selectedImage?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
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

@PreviewLightDarkWithBackground
@Composable
private fun NoResultsComponentPreview() {
    ReaderCollectionTheme {
        NoResultsComponent()
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun StartRatingBarPreview() {
    ReaderCollectionTheme {
        StarRatingBar(
            rating = 7f / 2,
            onRatingChanged = {},
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun SearchBarPreview() {
    ReaderCollectionTheme {
        SearchBar(text = "text", onSearch = {}, showLeadingIcon = true)
    }
}

@PreviewLightDarkWithBackground
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
                selectedImage = Icons.Default.Done,
            )
            CustomFilterChip(
                title = "Value 2",
                selected = false,
                onClick = {},
            )
            CustomFilterChip(
                title = "Value 3",
                selected = true,
                onClick = {},
                selectedIcon = R.drawable.ic_arrow_back,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun CustomChipPreview() {
    ReaderCollectionTheme {
        CustomChip("Value")
    }
}

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL, showBackground = true)
annotation class PreviewLightDarkWithBackground

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