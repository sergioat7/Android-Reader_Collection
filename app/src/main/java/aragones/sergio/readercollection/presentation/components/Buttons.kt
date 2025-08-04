/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 22/3/2024
 */

package aragones.sergio.readercollection.presentation.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@Composable
fun MainActionButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.MAIN,
) {
    val buttonColors = when (type) {
        ButtonType.MAIN -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.tertiary,
        )
        ButtonType.DESTRUCTIVE -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
        )
    }
    val textColor = when (type) {
        ButtonType.MAIN -> MaterialTheme.colorScheme.secondary
        ButtonType.DESTRUCTIVE -> MaterialTheme.colorScheme.onError
    }
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = buttonColors,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor,
            maxLines = 1,
        )
    }
}

@Composable
fun ListButton(painter: AccessibilityPainter, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.padding(12.dp),
        contentColor = MaterialTheme.colorScheme.secondary,
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        Icon(
            painter = painter.painter,
            contentDescription = painter.contentDescription,
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
fun MainTextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

@CustomPreviewLightDark
@Composable
private fun MainActionButtonPreview() {
    ReaderCollectionTheme {
        MainActionButton(
            text = stringResource(R.string.sign_in),
            enabled = true,
            onClick = {},
        )
    }
}

@CustomPreviewLightDark
@Composable
private fun MainDestructiveActionButtonPreview() {
    ReaderCollectionTheme {
        MainActionButton(
            text = stringResource(R.string.delete),
            enabled = true,
            onClick = {},
            type = ButtonType.DESTRUCTIVE,
        )
    }
}

@CustomPreviewLightDark
@Composable
private fun ListButtonPreview() {
    ReaderCollectionTheme {
        ListButton(
            painter = painterResource(R.drawable.ic_double_arrow_up)
                .withDescription(null),
            onClick = {},
        )
    }
}

@CustomPreviewLightDarkWithBackground
@Composable
private fun MainTextButtonPreview() {
    ReaderCollectionTheme {
        MainTextButton(
            text = "Log-in",
            onClick = {},
        )
    }
}

enum class ButtonType {
    MAIN,
    DESTRUCTIVE,
}