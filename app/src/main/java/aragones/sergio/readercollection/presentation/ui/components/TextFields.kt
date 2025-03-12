/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.description
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.CustomInputType
import com.aragones.sergio.util.extensions.isNotBlank
import com.aragones.sergio.util.extensions.toDate
import java.util.TimeZone

@Composable
fun CustomOutlinedTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorTextId: Int? = null,
    labelText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    textColor: Color = MaterialTheme.colors.primary,
    @DrawableRes endIcon: Int? = null,
    inputType: CustomInputType? = CustomInputType.TEXT,
    isLastTextField: Boolean? = null,
    maxLength: Int = Integer.MAX_VALUE,
    maxLines: Int = Integer.MAX_VALUE,
    enabled: Boolean = true,
    onEndIconClicked: (() -> Unit)? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val showLabel = !enabled || text.isNotBlank() || placeholderText == null || isFocused

    val label: @Composable (() -> Unit)? = labelText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.h3.takeIf { placeholderText != null }
                    ?: MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error.takeIf { errorTextId != null }
                    ?: MaterialTheme.colors.description.takeIf { placeholderText != null }
                    ?: inputHintTextColor,
            )
        }
    }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error.takeIf { errorTextId != null }
                    ?: inputHintTextColor,
            )
        }
    }
    val trailingIcon: @Composable (() -> Unit)? = endIcon?.let {
        {
            IconButton(onClick = { onEndIconClicked?.invoke() }) {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                )
            }
        }
    }
    val keyboardAction = when (isLastTextField) {
        true -> ImeAction.Done
        false -> ImeAction.Next
        else -> ImeAction.Default
    }
    val keyboardOptions = when (inputType) {
        CustomInputType.NUMBER -> KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = keyboardAction,
        )
        CustomInputType.PASSWORD -> KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = keyboardAction,
        )
        else -> KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = keyboardAction,
        )
    }

    Column(modifier) {
        OutlinedTextField(
            value = text,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = MaterialTheme.colors.primaryVariant,
                disabledBorderColor = MaterialTheme.colors.primaryVariant.takeIf { enabled }
                    ?: Color.Transparent,
                errorBorderColor = MaterialTheme.colors.error,
                errorLabelColor = MaterialTheme.colors.error,
            ),
            textStyle = textStyle.copy(
                color = MaterialTheme.colors.error.takeIf { errorTextId != null } ?: textColor,
            ),
            label = label?.takeIf { showLabel },
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboard?.hide()
                    focusManager.clearFocus()
                },
            ),
            visualTransformation = if (endIcon == R.drawable.ic_show_password) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            singleLine = inputType != CustomInputType.MULTI_LINE_TEXT,
            maxLines = maxLines,
            enabled = enabled.takeIf { inputType != CustomInputType.DATE } ?: false,
            readOnly = inputType == CustomInputType.DATE,
            isError = errorTextId != null,
            onValueChange = { newText ->
                if (newText.length <= maxLength) {
                    onTextChanged(newText)
                }
            },
            interactionSource = interactionSource,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp, top = 5.dp, end = 5.dp, bottom = 0.dp),
        ) {
            if (errorTextId != null) {
                Text(
                    text = stringResource(id = errorTextId),
                    style = MaterialTheme.typography.body2,
                    color = MaterialTheme.colors.error,
                )
            }
            if (enabled && maxLength != Integer.MAX_VALUE) {
                Text(
                    text = "${text.length} / $maxLength",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colors.error.takeIf { errorTextId != null } ?: textColor,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

@Composable
fun MultilineCustomOutlinedTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorTextId: Int? = null,
    labelText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    textColor: Color = MaterialTheme.colors.primary,
    @DrawableRes endIcon: Int? = null,
    isLastTextField: Boolean? = null,
    maxLength: Int = Integer.MAX_VALUE,
    maxLines: Int = Integer.MAX_VALUE,
    enabled: Boolean = true,
    onEndIconClicked: (() -> Unit)? = null,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var showReadMore by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        CustomOutlinedTextField(
            text = text,
            onTextChanged = onTextChanged,
            errorTextId = errorTextId,
            labelText = labelText,
            placeholderText = placeholderText,
            inputHintTextColor = inputHintTextColor,
            textStyle = textStyle,
            textColor = textColor,
            endIcon = endIcon,
            inputType = CustomInputType.MULTI_LINE_TEXT,
            isLastTextField = isLastTextField,
            maxLength = maxLength,
            maxLines = Integer.MAX_VALUE.takeIf { !showReadMore } ?: maxLines,
            enabled = enabled,
            onEndIconClicked = onEndIconClicked,
        )
        if (showReadMore) {
            MainTextButton(
                text = stringResource(R.string.read_more),
                onClick = { showReadMore = !showReadMore },
                modifier = Modifier.offset(0.dp, (-12).dp),
            )
        }
    }
    BasicText(
        text = text,
        modifier = modifier.height(0.dp),
        onTextLayout = { textLayoutResult = it },
        style = textStyle,
    )

    LaunchedEffect(textLayoutResult) {
        showReadMore = (textLayoutResult?.lineCount ?: 0) >= maxLines
    }
}

@Composable
fun DateCustomOutlinedTextField(
    text: String,
    onTextChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colors.primaryVariant,
    @DrawableRes endIcon: Int? = null,
    enabled: Boolean = true,
    onEndIconClicked: (() -> Unit)? = null,
    language: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    CustomOutlinedTextField(
        text = text,
        onTextChanged = {},
        modifier = modifier
            .let {
                it
                    .clickable { showDatePicker = true }
                    .takeIf { enabled } ?: it
            },
        labelText = labelText,
        placeholderText = placeholderText,
        inputHintTextColor = inputHintTextColor,
        textColor = MaterialTheme.colors.description,
        endIcon = endIcon,
        inputType = CustomInputType.DATE,
        enabled = enabled,
        onEndIconClicked = onEndIconClicked,
    )
    if (showDatePicker) {
        val currentDateInMillis = text
            .toDate(
                format = language?.let { Constants.getDateFormatToShow(it) },
                language = language,
                timeZone = TimeZone.getTimeZone("UTC"),
            )?.time
        CustomDatePickerDialog(
            currentValue = currentDateInMillis,
            onDateSelected = onTextChanged,
            onDismiss = { showDatePicker = false },
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun TextOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Username",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Label",
            maxLength = 100,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun PasswordOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Password",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Label",
            endIcon = R.drawable.ic_show_password,
            maxLength = 100,
            onEndIconClicked = {},
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun DisabledOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Text",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Label",
            maxLength = 100,
            enabled = false,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun ErrorOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Incorrect text",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            errorTextId = R.string.invalid_username,
            labelText = "Label",
            maxLength = 100,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun LargeCustomOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        MultilineCustomOutlinedTextField(
            text =
            """
            Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor
            incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud
            exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute
            irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla
            pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia
            deserunt mollit anim id est laborum.
            """.trimIndent(),
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Label",
            maxLength = 100,
        )
    }
}

@PreviewLightDarkWithBackground
@Composable
private fun DateCustomOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        DateCustomOutlinedTextField(
            text = "01/01/2000",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Label",
            enabled = true,
        )
    }
}