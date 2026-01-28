/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.isAndroid
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.SyncAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.automatic_sync_description
import reader_collection.app.generated.resources.automatic_sync_title
import reader_collection.app.generated.resources.data_sync_title
import reader_collection.app.generated.resources.manual_sync_description
import reader_collection.app.generated.resources.manual_sync_title
import reader_collection.app.generated.resources.sync_now_action
import reader_collection.app.generated.resources.syncing_options_title

@Composable
fun DataSyncScreen(
    state: DataSyncUiState,
    onBack: () -> Unit,
    onChange: (Boolean) -> Unit,
    onSync: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DataSyncToolbar(scrollState = scrollState, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
        ) {
            if (isAndroid()) {
                Spacer(Modifier.height(12.dp))
                HeaderText(text = stringResource(Res.string.syncing_options_title))
                SyncAutomaticallyItem(
                    isEnabled = state.isAutomaticSyncEnabled,
                    onChange = onChange,
                )
            }
            Spacer(Modifier.height(12.dp))
            SyncManuallyItem(onSync = onSync)
        }
    }
    if (state.isLoading) {
        SyncAlertDialog()
    }
}

@Composable
private fun DataSyncToolbar(scrollState: ScrollState, onBack: (() -> Unit)) {
    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(Res.string.data_sync_title),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
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
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun SyncAutomaticallyItem(isEnabled: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.automatic_sync_title),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.automatic_sync_description),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isEnabled,
            onCheckedChange = onChange,
            thumbContent = {
                Icon(
                    imageVector = if (isEnabled) {
                        Icons.Default.CloudDone
                    } else {
                        Icons.Default.CloudOff
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedIconColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                uncheckedIconColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            ),
        )
    }
}

@Composable
private fun SyncManuallyItem(onSync: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.manual_sync_title),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.manual_sync_description),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onSync,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text(
                text = stringResource(Res.string.sync_now_action),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
            )
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun DataSyncScreenPreview(
    @PreviewParameter(DataSyncScreenPreviewParameterProvider::class) state: DataSyncUiState,
) {
    ReaderCollectionTheme {
        DataSyncScreen(
            state = state,
            onBack = {},
            onChange = {},
            onSync = {},
        )
    }
}

private class DataSyncScreenPreviewParameterProvider :
    PreviewParameterProvider<DataSyncUiState> {

    override val values: Sequence<DataSyncUiState>
        get() = sequenceOf(
            DataSyncUiState(
                isAutomaticSyncEnabled = false,
                isLoading = false,
            ),
            DataSyncUiState(
                isAutomaticSyncEnabled = true,
                isLoading = false,
            ),
            DataSyncUiState(
                isAutomaticSyncEnabled = false,
                isLoading = true,
            ),
        )
}