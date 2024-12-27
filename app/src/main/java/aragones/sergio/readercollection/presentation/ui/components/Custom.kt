/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/5/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Configuration.UI_MODE_TYPE_NORMAL
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.lightRoseBud
import aragones.sergio.readercollection.presentation.ui.theme.roseBud

@Composable
fun NoResultsComponent(text: String = stringResource(id = R.string.no_results_text)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.image_no_results),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f),
        )
        Text(
            text = text,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp),
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
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
                i <= rating -> Pair(R.drawable.ic_round_star_24, MaterialTheme.colors.roseBud)
                i.toFloat() == rating + 0.5f -> Pair(
                    R.drawable.ic_round_star_half_24,
                    MaterialTheme.colors.roseBud,
                )
                else -> Pair(R.drawable.ic_round_star_border_24, MaterialTheme.colors.lightRoseBud)
            }
            Icon(
                painter = painterResource(id = icon),
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
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    textColor: Color = MaterialTheme.colors.primary,
    textStyle: TextStyle = MaterialTheme.typography.body1,
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

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        if (requestFocusByDefault) focusRequester.requestFocus()
    }

    val placeholder: @Composable (() -> Unit) = {
        Text(
            text = stringResource(R.string.search),
            color = inputHintTextColor,
            style = MaterialTheme.typography.body1,
        )
    }
    val leadingIcon: @Composable (() -> Unit)? = if (showLeadingIcon) {
        {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colors.primary,
            )
        }
    } else {
        null
    }
    val trailingIcon: @Composable (() -> Unit)? = if (textFieldValueState.text.isNotBlank()) {
        {
            TopAppBarIcon(
                icon = R.drawable.ic_clear_text,
                onClick = { textFieldValueState = textFieldValueState.copy("") },
            )
        }
    } else {
        null
    }

    OutlinedTextField(
        value = textFieldValueState,
        modifier = modifier.focusRequester(focusRequester),
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = MaterialTheme.colors.primary,
            unfocusedBorderColor = MaterialTheme.colors.primaryVariant,
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
                focusRequester.freeFocus()
                onSearch(textFieldValueState.text)
            },
        ),
        singleLine = true,
        onValueChange = {
            textFieldValueState = it
        },
    )
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

@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.FUNCTION,
)
@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL, showBackground = true)
annotation class PreviewLightDarkWithBackground