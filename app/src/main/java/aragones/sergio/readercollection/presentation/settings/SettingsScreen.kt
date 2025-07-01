/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@Composable
fun SettingsScreen(isLoading: Boolean, onLogout: () -> Unit, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(modifier = modifier.fillMaxSize()) {
        SettingsToolbar(scrollState = scrollState)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(12.dp))
            HeaderText(text = stringResource(R.string.account_title))
            SettingItem(
                icon = Icons.Default.AccountCircle,
                title = stringResource(R.string.account_title),
                subtitle = stringResource(R.string.account_description),
                onClick = {},
            )
            Spacer(modifier = Modifier.height(16.dp))
            HeaderText(text = stringResource(R.string.preferences))
            SettingItem(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.data_sync_title),
                subtitle = stringResource(R.string.data_sync_description),
                onClick = {},
            )
            SettingItem(
                icon = Icons.Default.DisplaySettings,
                title = stringResource(R.string.display_settings_title),
                subtitle = stringResource(R.string.display_settings_description),
                onClick = {},
            )
            SettingItem(
                icon = Icons.AutoMirrored.Default.Logout,
                title = stringResource(R.string.logout_title),
                subtitle = stringResource(R.string.logout_description),
                onClick = onLogout,
            )
        }
    }
    if (isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun SettingsToolbar(scrollState: ScrollState) {
    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(R.string.title_settings),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
    )
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
fun SettingItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsScreenPreviewParameterProvider::class) isLoading: Boolean,
) {
    ReaderCollectionTheme {
        SettingsScreen(
            isLoading = isLoading,
            onLogout = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}

private class SettingsScreenPreviewParameterProvider :
    PreviewParameterProvider<Boolean> {

    override val values: Sequence<Boolean>
        get() = sequenceOf(false, true)
}