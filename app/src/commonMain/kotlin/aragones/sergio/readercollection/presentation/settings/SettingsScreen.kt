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
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.account_description
import reader_collection.app.generated.resources.account_title
import reader_collection.app.generated.resources.data_sync_description
import reader_collection.app.generated.resources.data_sync_title
import reader_collection.app.generated.resources.display_settings_description
import reader_collection.app.generated.resources.display_settings_title
import reader_collection.app.generated.resources.friends_description
import reader_collection.app.generated.resources.friends_title
import reader_collection.app.generated.resources.logout_description
import reader_collection.app.generated.resources.logout_title
import reader_collection.app.generated.resources.preferences
import reader_collection.app.generated.resources.title_settings
import reader_collection.app.generated.resources.version

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onClickOption: (SettingsOption) -> Unit,
    modifier: Modifier = Modifier,
) {
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
            HeaderText(text = stringResource(Res.string.account_title))
            SettingItem(
                icon = Icons.Default.AccountCircle,
                title = stringResource(Res.string.account_title),
                subtitle = stringResource(Res.string.account_description),
                onClick = {
                    onClickOption(SettingsOption.Account)
                },
            )
            SettingItem(
                icon = Icons.Default.SupervisorAccount,
                title = stringResource(Res.string.friends_title),
                subtitle = stringResource(Res.string.friends_description),
                onClick = {
                    onClickOption(SettingsOption.Friends)
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            HeaderText(text = stringResource(Res.string.preferences))
            SettingItem(
                icon = Icons.Default.Backup,
                title = stringResource(Res.string.data_sync_title),
                subtitle = stringResource(Res.string.data_sync_description),
                onClick = {
                    onClickOption(SettingsOption.DataSync)
                },
            )
            SettingItem(
                icon = Icons.Default.DisplaySettings,
                title = stringResource(Res.string.display_settings_title),
                subtitle = stringResource(Res.string.display_settings_description),
                onClick = {
                    onClickOption(SettingsOption.DisplaySettings)
                },
            )
            SettingItem(
                icon = Icons.AutoMirrored.Default.Logout,
                title = stringResource(Res.string.logout_title),
                subtitle = stringResource(Res.string.logout_description),
                onClick = {
                    onClickOption(SettingsOption.Logout)
                },
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = stringResource(Res.string.version, state.version),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
    if (state.isLoading) {
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
        title = stringResource(Res.string.title_settings),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
    )
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
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
            state = SettingsUiState("1.0.0", false),
            onClickOption = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
        )
    }
}

private class SettingsScreenPreviewParameterProvider :
    PreviewParameterProvider<Boolean> {

    override val values: Sequence<Boolean>
        get() = sequenceOf(false, true)
}