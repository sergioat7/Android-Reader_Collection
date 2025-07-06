/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/3/2024
 */

package aragones.sergio.readercollection.presentation.components

import android.content.res.Configuration
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import aragones.sergio.readercollection.presentation.theme.roseBud
import java.util.Date

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
                color = MaterialTheme.colorScheme.background,
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
                        text = stringResource(textId),
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
                color = MaterialTheme.colorScheme.background,
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
                color = MaterialTheme.colorScheme.background,
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
                        text = stringResource(titleTextId),
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = MaterialTheme.shapes.medium,
                            ).testTag("textField"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.tertiary,
                            focusedTextColor = MaterialTheme.colorScheme.description,
                            unfocusedTextColor = MaterialTheme.colorScheme.description,
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

        val isPortrait =
            LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

        Dialog(
            onDismissRequest = { onCancel() },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth(if (isPortrait) 0.9f else 0.5f)
                    .testTag("sortingPickerAlertDialog"),
            ) {
                Column {
                    Spacer(Modifier.height(24.dp))
                    TextTitleAlertDialog(
                        text = stringResource(R.string.order_by),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    currentValue: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = currentValue,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButtonAlertDialog(
                text = stringResource(R.string.accept),
                onClick = {
                    onDateSelected(datePickerState.selectedDateMillis ?: Date().time)
                    onDismiss()
                },
            )
        },
        modifier = modifier,
        dismissButton = {
            TextButtonAlertDialog(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
            )
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = stringResource(R.string.select_a_date),
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            },
            showModeToggle = false,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.primary,
                weekdayContentColor = MaterialTheme.colorScheme.primary,
                subheadContentColor = MaterialTheme.colorScheme.primary,
                navigationContentColor = MaterialTheme.colorScheme.primary,
                yearContentColor = MaterialTheme.colorScheme.primary,
                disabledYearContentColor = MaterialTheme.colorScheme.primary,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.primary,
                disabledSelectedYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContainerColor = MaterialTheme.colorScheme.roseBud,
                disabledSelectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.primary,
                disabledDayContentColor = MaterialTheme.colorScheme.primary,
                selectedDayContentColor = MaterialTheme.colorScheme.primary,
                disabledSelectedDayContentColor = MaterialTheme.colorScheme.primary,
                selectedDayContainerColor = MaterialTheme.colorScheme.roseBud,
                disabledSelectedDayContainerColor = MaterialTheme.colorScheme.primary,
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.primary,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary,
                dividerColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}

@Composable
fun SyncAlertDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.testTag("syncAlertDialog"),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(24.dp))
                TextMessageAlertDialog(
                    text = stringResource(R.string.sync_title),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TextTitleAlertDialog(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun TextMessageAlertDialog(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.primary,
        lineHeight = 24.sp,
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
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@CustomPreviewLightDark
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

@CustomPreviewLightDark
@Composable
private fun InformationAlertDialogPreview() {
    ReaderCollectionTheme {
        InformationAlertDialog(
            show = true,
            text = stringResource(R.string.book_saved),
            onDismiss = {},
        )
    }
}

@CustomPreviewLightDark
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

@CustomPreviewLightDark
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

@CustomPreviewLightDark
@Composable
private fun CustomDatePickerDialogPreview() {
    ReaderCollectionTheme {
        CustomDatePickerDialog(
            currentValue = Date().time,
            onDateSelected = {},
            onDismiss = {},
        )
    }
}

@CustomPreviewLightDark
@Composable
private fun SyncAlertDialogPreview() {
    ReaderCollectionTheme {
        SyncAlertDialog()
    }
}