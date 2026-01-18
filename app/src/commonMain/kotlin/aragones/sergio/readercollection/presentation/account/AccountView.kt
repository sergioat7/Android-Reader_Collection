/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.profile_delete_confirmation
import reader_collection.app.generated.resources.public_profile_disable_confirmation
import reader_collection.app.generated.resources.username_info

@Composable
fun AccountView(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AccountViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()
    val error by viewModel.profileError.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    val logOut by viewModel.logOut.collectAsState()
    if (logOut) {
        onLogout()
        return
    }

    ReaderCollectionApp {
        AccountScreen(
            state = state,
            onShowInfo = {
                viewModel.showInfoDialog(Res.string.username_info)
            },
            onProfileDataChange = viewModel::profileDataChanged,
            onBack = onBack,
            onSave = viewModel::save,
            onChangePublicProfile = { enable ->
                if (enable) {
                    viewModel.setPublicProfile(true)
                } else {
                    viewModel.showConfirmationDialog(Res.string.public_profile_disable_confirmation)
                }
            },
            onDeleteAccount = {
                viewModel.showConfirmationDialog(Res.string.profile_delete_confirmation)
            },
        )
    }

    ConfirmationAlertDialog(
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            when (confirmationMessageId) {
                Res.string.profile_delete_confirmation -> {
                    viewModel.deleteUser()
                }
                Res.string.public_profile_disable_confirmation -> {
                    viewModel.setPublicProfile(false)
                }
                null -> {
                    /*no-op*/
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
    } else if (infoDialogMessageId != null) {
        stringResource(requireNotNull(infoDialogMessageId))
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