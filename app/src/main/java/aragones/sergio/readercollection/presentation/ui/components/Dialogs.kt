/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.description

@Composable
fun ConfirmationAlertDialog(
    show: Boolean,
    @StringRes textId: Int,
    onCancel: () -> Unit,
    onAccept: () -> Unit,
) {
    if (show) {
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                color = MaterialTheme.colors.background,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("confirmationAlertDialog"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Spacer(Modifier.height(24.dp))
                    TextMessageAlertDialog(
                        text = stringResource(id = textId),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButtonAlertDialog(
                            text = stringResource(R.string.cancel),
                            onClick = onCancel,
                        )
                        TextButtonAlertDialog(
                            text = stringResource(R.string.accept),
                            onClick = onAccept,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InformationAlertDialog(show: Boolean, text: String, onDismiss: () -> Unit) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                color = MaterialTheme.colors.background,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("informationAlertDialog"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Spacer(Modifier.height(24.dp))
                    TextMessageAlertDialog(
                        text = text,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    TextButtonAlertDialog(
                        text = stringResource(R.string.accept),
                        modifier = Modifier.align(Alignment.End),
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
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

        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                color = MaterialTheme.colors.background,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("textFieldAlertDialog"),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Spacer(Modifier.height(24.dp))
                    TextTitleAlertDialog(
                        text = stringResource(id = titleTextId),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(
                                BorderStroke(1.dp, MaterialTheme.colors.primary),
                                shape = MaterialTheme.shapes.medium,
                            ).testTag("textField"),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = MaterialTheme.colors.primary,
                            unfocusedBorderColor = MaterialTheme.colors.primaryVariant,
                            textColor = MaterialTheme.colors.description,
                        ),
                        shape = MaterialTheme.shapes.medium,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = type),
                    )
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButtonAlertDialog(
                            text = stringResource(R.string.cancel),
                            onClick = onCancel,
                        )
                        TextButtonAlertDialog(
                            text = stringResource(R.string.accept),
                            onClick = { onAccept(text) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortingPickerAlertDialog(
    state: UiSortingPickerState,
    onCancel: () -> Unit,
    onAccept: (newSortParam: String?, newIsSortDescending: Boolean) -> Unit,
) {
    if (state.show) {
        var newSortParam by rememberSaveable { mutableStateOf(state.sortParam) }
        var newIsSortDescending by rememberSaveable { mutableStateOf(state.isSortDescending) }

        val context = LocalContext.current
        val sortParamKeys = context.resources.getStringArray(R.array.sorting_param_keys).toList()
        val sortParamValues =
            context.resources.getStringArray(R.array.sorting_param_values).toList()
        val sortOrderValues =
            context.resources.getStringArray(R.array.sorting_order_values).toList()

        Dialog(
            onDismissRequest = { onCancel() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            Surface(
                color = MaterialTheme.colors.background,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.testTag("sortingPickerAlertDialog"),
            ) {
                Column {
                    Spacer(Modifier.height(24.dp))
                    TextTitleAlertDialog(
                        text = stringResource(id = R.string.order_by),
                        modifier = Modifier.padding(horizontal = 24.dp),
                    )
                    Row(horizontalArrangement = Arrangement.Center) {
                        Picker(
                            items = sortParamValues,
                            onSelect = {
                                newSortParam = sortParamKeys[it]
                            },
                            modifier = Modifier.weight(1f),
                            currentIndexSelected = state.sortParam?.let {
                                sortParamKeys.indexOf(it)
                            } ?: 0,
                        )
                        Picker(
                            items = sortOrderValues,
                            onSelect = {
                                newIsSortDescending = it == 1
                            },
                            modifier = Modifier.weight(1f),
                            currentIndexSelected = if (newIsSortDescending) 1 else 0,
                        )
                    }
                    Row(modifier = Modifier.align(Alignment.End)) {
                        TextButtonAlertDialog(
                            text = stringResource(R.string.cancel),
                            onClick = onCancel,
                        )
                        TextButtonAlertDialog(
                            text = stringResource(R.string.accept),
                            onClick = {
                                onAccept(newSortParam, newIsSortDescending)
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                }
            }
        }
    }
}

data class UiSortingPickerState(
    val show: Boolean,
    val sortParam: String?,
    val isSortDescending: Boolean,
)

@Composable
private fun TextTitleAlertDialog(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.h2,
        color = MaterialTheme.colors.primary,
    )
}

@Composable
private fun TextMessageAlertDialog(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.primary,
    )
}

@Composable
private fun TextButtonAlertDialog(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.testTag("textButtonAlertDialog"),
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.h3,
            color = MaterialTheme.colors.primary,
        )
    }
}

@PreviewLightDark
@Composable
private fun ConfirmationAlertDialogPreview() {
    ReaderCollectionTheme {
        ConfirmationAlertDialog(
            show = true,
            textId = R.string.book_remove_confirmation,
            onCancel = {},
            onAccept = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun InformationAlertDialogPreview() {
    ReaderCollectionTheme {
        InformationAlertDialog(
            show = true,
            text = stringResource(id = R.string.book_saved),
            onDismiss = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun TextFieldAlertDialogPreview() {
    ReaderCollectionTheme {
        TextFieldAlertDialog(
            show = true,
            titleTextId = R.string.enter_valid_url,
            type = KeyboardType.Uri,
            onCancel = {},
            onAccept = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun SortingPickerAlertDialogPreview() {
    ReaderCollectionTheme {
        SortingPickerAlertDialog(
            state = UiSortingPickerState(
                show = true,
                sortParam = "readingDate",
                isSortDescending = false,
            ),
            onCancel = {},
            onAccept = { _, _ -> },
        )
    }
}