/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    currentValue: String,
    values: List<String>,
    labelText: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String? = null,
    inputHintTextColor: Color = MaterialTheme.colorScheme.tertiary,
    textColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val contentDescription = stringResource(
        R.string.dropdown_text_field_description,
        labelText,
        currentValue,
    )

    val label: @Composable () -> Unit =
        {
            Text(
                text = labelText,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = inputHintTextColor,
            )
        }
    }
    val trailingIcon: @Composable (() -> Unit) =
        {
            Icon(
                imageVector = if (expanded) {
                    Icons.Default.KeyboardArrowUp
                } else {
                    Icons.Default.KeyboardArrowDown
                },
                contentDescription = null,
                tint = textColor,
            )
        }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = currentValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
                    .clearAndSetSemantics {
                        this.contentDescription = contentDescription
                        role = Role.DropdownList
                    },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.tertiary.takeIf { enabled }
                        ?: Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                label = label,
                placeholder = placeholder,
                trailingIcon = trailingIcon.takeIf { enabled },
                singleLine = true,
                enabled = false,
                readOnly = true,
                onValueChange = {},
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                ),
            ) {
                for (value in values) {
                    val itemContentDescription = stringResource(
                        R.string.dropdown_item_description,
                        values.indexOf(value) + 1,
                        values.size,
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                            )
                        },
                        onClick = {
                            expanded = false
                            onOptionSelected(value)
                        },
                        modifier = Modifier.semantics {
                            this.contentDescription = itemContentDescription
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun CustomDropdownMenuPreview() {
    ReaderCollectionTheme {
        CustomDropdownMenu(
            currentValue = "Value",
            values = listOf("Option 1", "Option 2", "Option 3"),
            onOptionSelected = {},
            modifier = Modifier.padding(12.dp),
            labelText = "Header",
            placeholderText = "Please choose",
        )
    }
}