/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@Composable
fun MainActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.tertiary,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.button,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
    }
}

@Composable
fun ListButton(@DrawableRes image: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        contentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(
            painter = painterResource(image),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
fun MainTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.button,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

@PreviewLightDark
@Composable
private fun MainActionButtonPreview() {
    ReaderCollectionTheme {
        MainActionButton(
            text = "Log-in",
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

@PreviewLightDarkWithBackground
@Composable
private fun MainTextButtonPreview() {
    ReaderCollectionTheme {
        MainTextButton(
            text = "Log-in",
            onClick = {},
        )
    }
}