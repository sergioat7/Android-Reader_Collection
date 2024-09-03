/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R

@Preview
@Composable
fun CustomToolbarPreview() {

    CustomToolbar(
        title = "Toolbar",
        onBack = {},
        actions = {
            IconButton(onClick = {}) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sort_books),
                    contentDescription = ""
                )
            }
        },
    )
}

@Composable
fun CustomToolbar(
    title: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h1,
                color = MaterialTheme.colors.primary,
            )
        },
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = elevation,
        navigationIcon = onBack?.let {
            {
                IconButton(onClick = { it.invoke() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
        },
        actions = actions,
    )
}

@Preview
@Composable
fun CustomSearchBarPreview() {
    CustomSearchBar(
        title = stringResource(id = R.string.title_search),
        query = "",
        onBack = {},
        onSearch = {},
    )
}

@Composable
fun CustomSearchBar(
    title: String,
    query: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    elevation: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
    onSearch: ((String) -> Unit),
) {

    var isSearching by remember { mutableStateOf(false) }

    val backIcon: @Composable (() -> Unit)? = if (isSearching || onBack != null) {
        {
            IconButton(onClick = {
                if (isSearching) {
                    isSearching = false
                } else {
                    onBack?.invoke()
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    } else null

    TopAppBar(
        title = {
            if (isSearching) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SearchBar(
                        text = query,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        onSearch = {
                            isSearching = false
                            if (it.isNotBlank()) {
                                onSearch(it)
                            }
                        },
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
                IconButton(onClick = {
                    isSearching = true
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "",
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
        }
    )
}

@Composable
fun SearchBar(
    text: String,
    modifier: Modifier = Modifier,
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    textColor: Color = MaterialTheme.colors.primary,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    onSearch: (String) -> Unit,
) {

    var textFieldValueState by remember {
        mutableStateOf(
            TextFieldValue(
                text = text,
                selection = when {
                    text.isEmpty() -> TextRange.Zero
                    else -> TextRange(text.length, text.length)
                }
            )
        )
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val placeholder: @Composable (() -> Unit) = {
        Text(
            text = stringResource(R.string.search),
            color = inputHintTextColor,
            style = MaterialTheme.typography.body1,
        )
    }
    val trailingIcon: @Composable (() -> Unit)? = if (textFieldValueState.text.isNotBlank()) {
        {
            IconButton(onClick = {
                textFieldValueState = textFieldValueState.copy("")
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_clear_text),
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    } else null

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
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(onSearch = {
            focusRequester.freeFocus()
            onSearch(textFieldValueState.text)
        }),
        singleLine = true,
        onValueChange = {
            textFieldValueState = it
        },
    )
}