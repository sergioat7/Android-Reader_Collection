/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme

@Composable
fun MainActionButton(
    text: String,
    modifier: Modifier,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            backgroundColor = MaterialTheme.colors.primary,
            disabledBackgroundColor = MaterialTheme.colors.primaryVariant,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.button,
            color = MaterialTheme.colors.secondary,
            maxLines = 1,
        )
    }
}

@Composable
fun ListButton(
    @DrawableRes image: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        contentColor = MaterialTheme.colors.secondary,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        Icon(
            painter = painterResource(id = image),
            contentDescription = null,
            tint = MaterialTheme.colors.secondary,
        )
    }
}

@PreviewLightDark
@Composable
private fun MainActionButtonPreview() {
    ReaderCollectionTheme {
        MainActionButton(
            text = "Log-in",
            modifier = Modifier,
            enabled = true,
            onClick = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ListButtonPreview() {
    ReaderCollectionTheme {
        ListButton(
            image = R.drawable.ic_double_arrow_up,
            onClick = {},
        )
    }
}