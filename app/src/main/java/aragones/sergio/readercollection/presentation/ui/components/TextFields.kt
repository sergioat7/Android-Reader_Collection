/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import com.aragones.sergio.util.CustomInputType

@Preview(showBackground = true)
@Composable
fun TextOutlinedTextFieldPreview() {
    CustomOutlinedTextField(
        text = "Username",
        modifier = Modifier.padding(12.dp),
        labelText = "Label",
        onTextChanged = {})
}

@Preview(showBackground = true)
@Composable
fun PasswordOutlinedTextFieldPreview() {
    CustomOutlinedTextField(
        text = "Password",
        modifier = Modifier.padding(12.dp),
        labelText = "Label",
        endIcon = R.drawable.ic_show_password,
        onTextChanged = {},
        onEndIconClicked = {}
    )
}

@Preview(showBackground = true)
@Composable
fun DisabledOutlinedTextFieldPreview() {
    CustomOutlinedTextField(
        text = "Text",
        modifier = Modifier.padding(12.dp),
        labelText = "Label",
        enabled = false,
        onTextChanged = {}
    )
}

@Preview(showBackground = true)
@Composable
fun ErrorOutlinedTextFieldPreview() {
    CustomOutlinedTextField(
        text = "Incorrect text",
        errorTextId = R.string.invalid_username,
        modifier = Modifier.padding(12.dp),
        labelText = "Label",
        onTextChanged = {}
    )
}

@Composable
fun CustomOutlinedTextField(
    text: String,
    errorTextId: Int? = null,
    modifier: Modifier,
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
    onTextChanged: (String) -> Unit,
    onEndIconClicked: (() -> Unit)? = null
) {

    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val margin5 = dimensionResource(id = R.dimen.margin_5dp).value

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
                    contentDescription = "",
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
            imeAction = keyboardAction
        )

        CustomInputType.PASSWORD -> KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = keyboardAction
        )

        else -> KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = keyboardAction
        )
    }

    Column(modifier = modifier) {

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
            textStyle = textStyle.copy(color = if (errorTextId != null) MaterialTheme.colors.error else textColor),
            label = label,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            keyboardOptions = keyboardOptions,
            keyboardActions = KeyboardActions(onDone = {

                keyboard?.hide()
                focusManager.clearFocus()
            }),
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
            Text(
                text = stringResource(id = errorTextId),
                modifier = Modifier.padding(start = margin5.dp, top = margin5.dp),
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error,
            )
        }
    }
}