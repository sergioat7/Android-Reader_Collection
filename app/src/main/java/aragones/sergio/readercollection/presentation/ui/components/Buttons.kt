/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R

@Preview
@Composable
fun CustomButtonPreview() {
    MainActionButton(text = "Log-in", modifier = Modifier, enabled = true) {}
}

@Composable
fun MainActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = { onClick() },
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
        ),
        shape = RoundedCornerShape(15.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colors.secondary,
            fontFamily = robotoSerifFamily,
            fontWeight = FontWeight.Bold,
            fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp,
            maxLines = 1,
        )
    }
}