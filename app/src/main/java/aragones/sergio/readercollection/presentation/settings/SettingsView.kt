/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun SettingsView(
    onRelaunch: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()
    val error by viewModel.profileError.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    val relaunch by viewModel.relaunch.collectAsState()
    if (relaunch) {
        onRelaunch()
        return
    }

    val logOut by viewModel.logOut.collectAsState()
    if (logOut) {
        onLogout()
        return
    }

    ReaderCollectionApp(navigationBarSameAsBackground = false) {
        SettingsScreen(
            state = state,
            onShowInfo = {
                viewModel.showInfoDialog(R.string.username_info)
            },
            onProfileDataChange = viewModel::profileDataChanged,
            onDeleteProfile = {
                viewModel.showConfirmationDialog(R.string.profile_delete_confirmation)
            },
            onLogout = {
                viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)
            },
            onSave = viewModel::save,
        )
    }

    ConfirmationAlertDialog(
        show = confirmationMessageId != -1,
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            when (confirmationMessageId) {
                R.string.profile_delete_confirmation -> {
                    viewModel.deleteUser()
                }
                R.string.profile_logout_confirmation -> {
                    viewModel.logout()
                }
                else -> {
                    Unit
                }
            }
            viewModel.closeDialogs()
        },
    )

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

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
}