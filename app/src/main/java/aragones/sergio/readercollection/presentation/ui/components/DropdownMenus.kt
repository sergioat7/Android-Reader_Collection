/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R

@Preview(showBackground = true)
@Composable
fun CustomDropdownMenuPreview() {
    CustomDropdownMenu(
        currentValue = "Value",
        labelText = "Header",
        placeholderText = "Please choose",
        modifier = Modifier.padding(12.dp),
        values = listOf("Option 1", "Option 2", "Option 3"),
        onOptionSelected = {}
    )
}

@Composable
fun CustomDropdownMenu(
    currentValue: String,
    modifier: Modifier,
    labelText: String? = null,
    placeholderText: String? = null,
    inputHintTextColor: Color = colorResource(id = R.color.textPrimaryLight),
    textColor: Color = colorResource(id = R.color.textPrimary),
    values: List<String>,
    onOptionSelected: (String) -> Unit
) {

    var expanded by rememberSaveable { mutableStateOf(false) }

    val textSize12 = dimensionResource(id = R.dimen.text_size_12sp).value
    val textSize16 = dimensionResource(id = R.dimen.text_size_16sp).value

    val label: @Composable (() -> Unit)? = labelText?.let {
        {
            Text(
                text = it,
                color = inputHintTextColor,
                fontFamily = robotoSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = textSize12.sp
            )
        }
    }
    val placeholder: @Composable (() -> Unit)? = placeholderText?.let {
        {
            Text(
                text = it,
                color = inputHintTextColor,
                fontFamily = robotoSerifFamily,
                fontWeight = FontWeight.Normal,
                fontSize = textSize12.sp
            )
        }
    }
    val trailingIcon: @Composable (() -> Unit) =
        {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = ""
                )
            }
        }

    Column(modifier = modifier) {

        OutlinedTextField(
            value = currentValue,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(10.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = colorResource(id = R.color.colorPrimary),
                unfocusedBorderColor = colorResource(id = R.color.colorPrimaryLight)
            ),
            textStyle = TextStyle(
                color = textColor,
                fontSize = textSize16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = robotoSerifFamily
            ),
            label = label,
            placeholder = placeholder,
            trailingIcon = trailingIcon,
            singleLine = true,
            enabled = false,
            readOnly = true,
            onValueChange = {
                onOptionSelected(it)
            }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier.fillMaxWidth()
        ) {
            for (value in values) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onOptionSelected(value)
                }) {
                    Text(
                        text = value,
                        color = textColor,
                        fontFamily = robotoSerifFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = textSize16.sp
                    )
                }
            }
        }
    }
}