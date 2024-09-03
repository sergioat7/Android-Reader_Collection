/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.presentation.ui.theme.description

@Preview(showBackground = true)
@Composable
fun CustomRadioButtonPreview() {
    CustomRadioButton(text = "Option", modifier = Modifier.padding(12.dp), selected = true) {}
}

@Composable
fun CustomRadioButton(
    text: String,
    modifier: Modifier,
    color: Color = MaterialTheme.colors.primary,
    textColor: Color = MaterialTheme.colors.description,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = { onClick() },
            colors = RadioButtonDefaults.colors(
                selectedColor = color,
                unselectedColor = color
            ),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.body1,
            color = textColor,
        )
    }
}