/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.CustomInputType

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

    val label: @Composable (() -> Unit)? = labelText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                color = if (errorTextId != null) MaterialTheme.colors.error else inputHintTextColor,
            )
        }
    }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.body2,
                color = if (errorTextId != null) MaterialTheme.colors.error else inputHintTextColor,
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
                disabledBorderColor = Color.Transparent,
                errorBorderColor = MaterialTheme.colors.error,
                errorLabelColor = MaterialTheme.colors.error,
            ),
            textStyle = textStyle.copy(
                color = if (errorTextId != null) MaterialTheme.colors.error else textColor,
            ),
            label = label,
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
            enabled = enabled,
            readOnly = inputType == CustomInputType.DATE,
            isError = errorTextId != null,
            onValueChange = { newText ->
                if (newText.length <= maxLength) {
                    onTextChanged(newText)
                }
            },
        )
        if (errorTextId != null) {
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(id = errorTextId),
                modifier = Modifier.padding(horizontal = 5.dp),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error,
            )
        }
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
        )
    }
}