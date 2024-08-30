/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import aragones.sergio.readercollection.R

val robotoSerifFamily = FontFamily(
    Font(R.font.roboto_serif_thin, FontWeight.Thin),
    Font(R.font.roboto_serif_regular, FontWeight.Normal),
    Font(R.font.roboto_serif_bold, FontWeight.Bold)
)

@Preview
@Composable
fun ConfirmationAlertDialogPreview() {
    ConfirmationAlertDialog(true, R.string.book_remove_confirmation, {}, {})
}

@Composable
fun ConfirmationAlertDialog(
    show: Boolean,
    @StringRes textId: Int,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    if (show) {

        val padding12 = dimensionResource(id = R.dimen.padding_12dp).value

        Dialog(
            onDismissRequest = { onCancel() },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                color = colorResource(id = R.color.colorSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("confirmationAlertDialog"),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TextMessageAlertDialog(stringResource(id = textId))
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = padding12.dp),
                    ) {
                        TextButtonAlertDialog(textId = R.string.cancel) {
                            onCancel()
                        }
                        TextButtonAlertDialog(textId = R.string.accept) {
                            onAccept()
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun InformationAlertDialogPreview() {
    InformationAlertDialog(true, stringResource(id = R.string.book_saved)) {}
}

@Composable
fun InformationAlertDialog(
    show: Boolean,
    text: String,
    onDismiss: () -> Unit,
) {
    if (show) {

        val padding12 = dimensionResource(id = R.dimen.padding_12dp).value

        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            Surface(
                color = colorResource(id = R.color.colorSecondary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("informationAlertDialog"),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TextMessageAlertDialog(text)
                    TextButtonAlertDialog(
                        textId = R.string.accept,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = padding12.dp),
                    ) {
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun TextFieldAlertDialogPreview() {
    TextFieldAlertDialog(true, R.string.enter_valid_url, type = KeyboardType.Uri, {}, {})
}

@Composable
fun TextFieldAlertDialog(
    show: Boolean,
    @StringRes titleTextId: Int,
    type: KeyboardType,
    onCancel: () -> Unit,
    onAccept: (String) -> Unit,
) {
    if (show) {

        var text by rememberSaveable { mutableStateOf("") }
        val padding12 = dimensionResource(id = R.dimen.padding_12dp).value
        val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
        val colorPrimary = colorResource(id = R.color.colorPrimary)
        val colorPrimaryLight = colorResource(id = R.color.colorPrimaryLight)
        val colorSecondary = colorResource(id = R.color.colorSecondary)
        val textSecondary = colorResource(id = R.color.textSecondary)

        Dialog(
            onDismissRequest = { onCancel() },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            Surface(
                color = colorSecondary,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("textFieldAlertDialog"),
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TextTitleAlertDialog(titleTextId)
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(start = padding24.dp, end = padding24.dp)
                            .border(
                                BorderStroke(1.dp, colorResource(id = R.color.colorPrimary)),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .testTag("textField"),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = colorPrimary,
                            unfocusedBorderColor = colorPrimaryLight,
                            textColor = textSecondary,
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = type),
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = padding12.dp),
                    ) {
                        TextButtonAlertDialog(textId = R.string.cancel) {
                            onCancel()
                        }
                        TextButtonAlertDialog(textId = R.string.accept) {
                            onAccept(text)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TextTitleAlertDialog(@StringRes textId: Int) {

    val padding8 = dimensionResource(id = R.dimen.padding_8dp).value
    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    Text(
        modifier = Modifier.padding(
            top = padding24.dp,
            start = padding24.dp,
            end = padding24.dp,
            bottom = padding8.dp,
        ),
        text = stringResource(id = textId),
        color = colorResource(id = R.color.colorPrimary),
        fontSize = dimensionResource(id = R.dimen.text_size_18sp).value.sp,
        fontFamily = robotoSerifFamily,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun TextMessageAlertDialog(text: String) {

    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    Text(
        modifier = Modifier.padding(
            top = padding24.dp,
            start = padding24.dp,
            end = padding24.dp,
        ),
        text = text,
        color = colorResource(id = R.color.colorPrimary),
        fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
        fontFamily = robotoSerifFamily,
        fontWeight = FontWeight.Normal,
    )
}

@Composable
fun TextButtonAlertDialog(
    @StringRes textId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {

    TextButton(
        onClick = { onClick() },
        modifier = modifier.testTag("textButtonAlertDialog"),
    ) {
        Text(
            text = stringResource(id = textId).uppercase(),
            color = colorResource(id = R.color.colorPrimary),
            fontSize = dimensionResource(id = R.dimen.text_size_14sp).value.sp,
            fontFamily = robotoSerifFamily,
            fontWeight = FontWeight.Bold,
        )
    }
}