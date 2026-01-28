/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.koin.compose.viewmodel.koinViewModel
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.profile_logout_confirmation

@Composable
fun SettingsView(
    onClickOption: (SettingsOption) -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()

    val logOut by viewModel.logOut.collectAsState()
    if (logOut) {
        onClickOption(SettingsOption.Logout)
        return
    }

    ReaderCollectionApp(navigationBarSameAsBackground = false) {
        SettingsScreen(
            state = state,
            onClickOption = {
                when (it) {
                    is SettingsOption.Account,
                    is SettingsOption.Friends,
                    is SettingsOption.DataSync,
                    is SettingsOption.DisplaySettings,
                    -> onClickOption(it)
                    is SettingsOption.Logout -> viewModel.showConfirmationDialog(
                        Res.string.profile_logout_confirmation,
                    )
                }
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
                Res.string.profile_logout_confirmation -> {
                    viewModel.logout()
                }
                null -> {
                    /*no-op*/
                }
            }
            viewModel.closeDialogs()
        },
    )

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
}