/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.dropdown_item_description

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDropdownMenu(
    values: DropdownValues,
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ExposedDropdownMenuBoxScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpand,
        ) {
            content()
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpand(false) },
                modifier = Modifier.background(
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.75f),
                ),
            ) {
                for (value in values.values) {
                    val itemContentDescription = stringResource(
                        Res.string.dropdown_item_description,
                        values.values.indexOf(value) + 1,
                        values.values.size,
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
                            onExpand(false)
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

@OptIn(ExperimentalMaterial3Api::class)
@CustomPreviewLightDarkWithBackground
@Composable
private fun CustomDropdownMenuPreview() {
    ReaderCollectionTheme {
        CustomDropdownMenu(
            values = DropdownValues(listOf("Option 1", "Option 2", "Option 3")),
            expanded = false,
            onExpand = {},
            onOptionSelected = {},
        ) {
            CustomInputChip(
                text = "Value",
                endIcon = rememberVectorPainter(Icons.Default.KeyboardArrowDown)
                    .withDescription(null),
            )
        }
    }
}

@Immutable
data class DropdownValues(val values: List<String>)