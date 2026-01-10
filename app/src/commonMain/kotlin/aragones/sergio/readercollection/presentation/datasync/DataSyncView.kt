/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.AppUiProvider.cancelWorker
import aragones.sergio.readercollection.presentation.theme.AppUiProvider.launchWorker
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.sync_confirmation

@Composable
fun DataSyncView(onBack: () -> Unit, viewModel: DataSyncViewModel = koinViewModel()) {
    val state by viewModel.state
    val error by viewModel.error.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()

    ReaderCollectionApp {
        DataSyncScreen(
            state = state,
            onBack = onBack,
            onChange = viewModel::changeAutomaticSync,
            onSync = {
                viewModel.showConfirmationDialog(Res.string.sync_confirmation)
            },
        )
    }

    if (state.isAutomaticSyncEnabled) {
        launchWorker()
    } else {
        cancelWorker()
    }

    val text = if (error != null) {
        val errorText = StringBuilder()
        if (requireNotNull(error).error.isNotEmpty()) {
            errorText.append(requireNotNull(error).error)
        } else {
            errorText.append(stringResource(requireNotNull(error).errorKey))
        }
        errorText.toString()
    } else if (infoDialogMessageId != null) {
        stringResource(requireNotNull(infoDialogMessageId))
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }

    ConfirmationAlertDialog(
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            when (confirmationMessageId) {
                Res.string.sync_confirmation -> {
                    viewModel.syncData()
                }
                null -> {
                    /*no-op*/
                }
            }
            viewModel.closeDialogs()
        },
    )
}