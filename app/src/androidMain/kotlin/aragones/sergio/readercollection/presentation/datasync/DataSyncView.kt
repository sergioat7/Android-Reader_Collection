/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 1/7/2025
 */

package aragones.sergio.readercollection.presentation.datasync

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import aragones.sergio.readercollection.utils.SyncDataWorker
import java.util.concurrent.TimeUnit
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

    val context = LocalContext.current

    if (state.isAutomaticSyncEnabled) {
        setupWorker(context)
    } else {
        WorkManager
            .getInstance(context)
            .cancelUniqueWork(SyncDataWorker.WORK_NAME)
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

private fun setupWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<SyncDataWorker>(7, TimeUnit.DAYS)
        .setConstraints(
            Constraints
                .Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        ).build()

    WorkManager
        .getInstance(context)
        .enqueueUniquePeriodicWork(
            uniqueWorkName = SyncDataWorker.WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.KEEP,
            request = workRequest,
        )
}