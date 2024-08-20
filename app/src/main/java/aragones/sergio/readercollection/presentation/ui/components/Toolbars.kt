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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        })
}

@Composable
fun CustomToolbar(
    title: String,
    modifier: Modifier = Modifier,
    elevation: Dp = 0.dp,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = TextStyle(
                    color = colorResource(id = R.color.textPrimary),
                    fontFamily = robotoSerifFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = dimensionResource(id = R.dimen.text_size_24sp).value.sp
                )
            )
        },
        modifier = modifier,
        backgroundColor = colorResource(id = R.color.colorSecondary),
        elevation = elevation,
        navigationIcon = onBack?.let {
            {
                IconButton(onClick = { it.invoke() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_back_blue),
                        contentDescription = ""
                    )
                }
            }
        },
        actions = actions
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
    backgroundColor: Color = colorResource(id = R.color.colorSecondary),
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
                    tint = colorResource(id = R.color.colorPrimary),
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
                            if(it.isNotBlank()) {
                                onSearch(it)
                            }
                        },
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                }
            } else {
                Text(
                    text = query.ifBlank { title },
                    style = TextStyle(
                        color = colorResource(id = R.color.textPrimary),
                        fontFamily = robotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.text_size_24sp).value.sp,
                    )
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
                        tint = colorResource(id = R.color.colorPrimary),
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
    inputHintTextColor: Color = colorResource(id = R.color.textPrimaryLight),
    textColor: Color = colorResource(id = R.color.textPrimary),
    fontSize: Float = dimensionResource(id = R.dimen.text_size_16sp).value,
    fontWeight: FontWeight = FontWeight.Normal,
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
            fontFamily = robotoSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
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
                    tint = colorResource(id = R.color.colorPrimary),
                )
            }
        }
    } else null

    OutlinedTextField(
        value = textFieldValueState,
        modifier = modifier.focusRequester(focusRequester),
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = colorResource(id = R.color.colorPrimary),
            unfocusedBorderColor = colorResource(id = R.color.colorPrimaryLight),
        ),
        textStyle = TextStyle(
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = fontWeight,
            fontFamily = robotoSerifFamily,
        ),
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
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