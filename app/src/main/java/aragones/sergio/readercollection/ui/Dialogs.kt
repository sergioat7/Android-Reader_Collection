/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/3/2024
 */

package aragones.sergio.readercollection.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
    onAccept: () -> Unit
) {
    if (show) {

        val padding12 = dimensionResource(id = R.dimen.padding_12dp).value

        Dialog(
            onDismissRequest = { onCancel() },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                color = colorResource(id = R.color.colorSecondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TextMessageAlertDialog(textId)
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = padding12.dp)
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
    InformationAlertDialog(true, R.string.book_saved) {}
}

@Composable
fun InformationAlertDialog(
    show: Boolean,
    @StringRes textId: Int,
    onDismiss: () -> Unit
) {
    if (show) {

        val padding12 = dimensionResource(id = R.dimen.padding_12dp).value

        Dialog(
            onDismissRequest = { onDismiss() },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                color = colorResource(id = R.color.colorSecondary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    TextMessageAlertDialog(textId)
                    TextButtonAlertDialog(
                        textId = R.string.accept,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = padding12.dp)
                    ) {
                        onDismiss()
                    }
                }
            }
        }
    }
}

@Composable
fun TextMessageAlertDialog(@StringRes textId: Int) {

    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    Text(
        modifier = Modifier.padding(
            top = padding24.dp,
            start = padding24.dp,
            end = padding24.dp
        ),
        text = stringResource(id = textId),
        color = colorResource(id = R.color.colorPrimary),
        fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
        fontFamily = robotoSerifFamily,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun TextButtonAlertDialog(
    @StringRes textId: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {

    TextButton(
        onClick = { onClick() },
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = textId).uppercase(),
            color = colorResource(id = R.color.colorPrimary),
            fontSize = dimensionResource(id = R.dimen.text_size_14sp).value.sp,
            fontFamily = robotoSerifFamily,
            fontWeight = FontWeight.Bold
        )
    }
}