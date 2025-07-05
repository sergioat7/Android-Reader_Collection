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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import aragones.sergio.readercollection.utils.SyncDataWorker
import java.util.concurrent.TimeUnit

@Composable
fun DataSyncView(onBack: () -> Unit, viewModel: DataSyncViewModel = hiltViewModel()) {
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
                viewModel.showConfirmationDialog(R.string.sync_confirmation)
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
    } else if (infoDialogMessageId != -1) {
        stringResource(infoDialogMessageId)
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }

    ConfirmationAlertDialog(
        show = confirmationMessageId != -1,
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            when (confirmationMessageId) {
                R.string.sync_confirmation -> {
                    viewModel.syncData()
                }
                else -> {
                    Unit
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