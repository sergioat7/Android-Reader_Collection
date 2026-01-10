/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.utils.UiDateMapper.toLong
import com.aragones.sergio.util.CustomInputType
import com.aragones.sergio.util.extensions.isNotBlank
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.field_required
import reader_collection.app.generated.resources.invalid_username
import reader_collection.app.generated.resources.read_more
import reader_collection.app.generated.resources.show_password

@Composable
fun CustomOutlinedTextField(
    text: String,
    labelText: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colorScheme.tertiary,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.primary,
    endIcon: AccessibilityPainter? = null,
    inputType: CustomInputType? = CustomInputType.TEXT,
    isLastTextField: Boolean? = null,
    maxLength: Int = Integer.MAX_VALUE,
    maxLines: Int = Integer.MAX_VALUE,
    enabled: Boolean = true,
    isRequired: Boolean = false,
    onEndIconClicked: (() -> Unit)? = null,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val showLabel = !enabled || text.isNotBlank() || placeholderText == null || isFocused

    val currentTextStyle = textStyle.copy(
        color = MaterialTheme.colorScheme.error.takeIf { errorText != null } ?: textColor,
        lineHeight = 24.sp,
    )
    val label: @Composable (() -> Unit) =
        {
            Text(
                text = labelText,
                style = MaterialTheme.typography.displaySmall.takeIf { placeholderText != null }
                    ?: MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error.takeIf { errorText != null }
                    ?: inputHintTextColor,
            )
        }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error.takeIf { errorText != null }
                    ?: inputHintTextColor,
            )
        }
    }
    val trailingIcon: @Composable (() -> Unit)? = endIcon?.let {
        {
            IconButton(onClick = { onEndIconClicked?.invoke() }) {
                Icon(
                    painter = endIcon.painter,
                    contentDescription = endIcon.contentDescription,
                    tint = MaterialTheme.colorScheme.primary.takeIf { errorText == null }
                        ?: MaterialTheme.colorScheme.error,
                )
            }
        }
    } ?: errorText?.let {
        {
            IconButton(onClick = { onEndIconClicked?.invoke() }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
    val supportingText: @Composable (() -> Unit) =
        {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 0.dp, top = 5.dp, end = 0.dp, bottom = 0.dp),
            ) {
                if (errorText != null) {
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                } else if (isRequired) {
                    Text(
                        text = stringResource(Res.string.field_required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.width(4.dp))
                if (enabled && maxLength != Integer.MAX_VALUE) {
                    Text(
                        text = "${text.length} / $maxLength",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                        ),
                        color = MaterialTheme.colorScheme.error.takeIf { errorText != null }
                            ?: textColor,
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
    val visualTransformation = if (endIcon?.contentDescription == stringResource(
            Res.string.show_password,
        )
    ) {
        PasswordVisualTransformation()
    } else {
        MarkdownTransformation(
            textStyle = currentTextStyle,
            backgroundColor = MaterialTheme.colorScheme.background,
        )
    }

    OutlinedTextField(
        value = text,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
            disabledBorderColor = MaterialTheme.colorScheme.tertiary.takeIf { enabled }
                ?: Color.Transparent,
            errorBorderColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error,
        ),
        textStyle = currentTextStyle,
        label = label.takeIf { showLabel } ?: placeholder,
        placeholder = placeholder.takeIf { showLabel },
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = {
                keyboard?.hide()
                focusManager.clearFocus()
            },
        ),
        visualTransformation = visualTransformation,
        singleLine = inputType != CustomInputType.MULTI_LINE_TEXT,
        maxLines = maxLines,
        enabled = enabled.takeIf { inputType != CustomInputType.DATE } ?: false,
        readOnly = inputType == CustomInputType.DATE,
        isError = errorText != null,
        onValueChange = { newText ->
            if (newText.length <= maxLength) {
                onTextChanged(newText)
            }
        },
        interactionSource = interactionSource,
    )
}

@Composable
fun MultilineCustomOutlinedTextField(
    text: String,
    labelText: String,
    onTextChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colorScheme.tertiary,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.primary,
    endIcon: AccessibilityPainter? = null,
    isLastTextField: Boolean? = null,
    maxLength: Int = Integer.MAX_VALUE,
    maxLines: Int = Integer.MAX_VALUE,
    enabled: Boolean = true,
    onEndIconClicked: (() -> Unit)? = null,
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var showReadMore by remember { mutableStateOf(false) }
    var readMoreHiddenByUser by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        CustomOutlinedTextField(
            text = text,
            labelText = labelText,
            onTextChanged = onTextChanged,
            modifier = Modifier.fillMaxWidth(),
            errorText = errorText,
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
                text = stringResource(Res.string.read_more),
                onClick = {
                    showReadMore = !showReadMore
                    readMoreHiddenByUser = true
                },
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
        if (!readMoreHiddenByUser) {
            showReadMore = (textLayoutResult?.lineCount ?: 0) >= maxLines
        }
    }
}

@Composable
fun DateCustomOutlinedTextField(
    text: String,
    labelText: String,
    onTextChanged: (Long) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colorScheme.tertiary,
    endIcon: AccessibilityPainter? = null,
    enabled: Boolean = true,
    onEndIconClicked: (() -> Unit)? = null,
    language: String? = null,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    CustomOutlinedTextField(
        text = text,
        labelText = labelText,
        onTextChanged = {},
        modifier = modifier
            .fillMaxWidth()
            .let {
                it
                    .clickable { showDatePicker = true }
                    .takeIf { enabled } ?: it
            },
        placeholderText = placeholderText,
        inputHintTextColor = inputHintTextColor,
        textColor = MaterialTheme.colorScheme.primary,
        endIcon = endIcon,
        inputType = CustomInputType.DATE,
        enabled = enabled,
        onEndIconClicked = onEndIconClicked,
    )
    if (showDatePicker) {
        CustomDatePickerDialog(
            currentValue = text.toLong(language),
            onDateSelected = onTextChanged,
            onDismiss = { showDatePicker = false },
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun TextOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Username",
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            maxLength = 100,
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun PasswordOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Password",
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            endIcon = painterResource(R.drawable.ic_show_password)
                .withDescription(null),
            isRequired = true,
            onEndIconClicked = {},
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun DisabledOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Text",
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            maxLength = 100,
            enabled = false,
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun ErrorOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        CustomOutlinedTextField(
            text = "Incorrect text",
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            errorText = stringResource(Res.string.invalid_username),
            maxLength = 100,
        )
    }
}

@CustomPreviewLightDarkWithBackground
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
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            maxLength = 500,
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun DateCustomOutlinedTextFieldPreview() {
    ReaderCollectionTheme {
        DateCustomOutlinedTextField(
            text = "01/01/2000",
            labelText = "Label",
            onTextChanged = {},
            modifier = Modifier.padding(12.dp),
            enabled = true,
        )
    }
}