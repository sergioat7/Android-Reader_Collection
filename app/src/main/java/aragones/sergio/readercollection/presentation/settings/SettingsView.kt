/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/1/2025
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun SettingsView(onLogout: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val isLoading by viewModel.isLoading
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()

    val logOut by viewModel.logOut.collectAsState()
    if (logOut) {
        onLogout()
        return
    }

    ReaderCollectionApp(navigationBarSameAsBackground = false) {
        SettingsScreen(
            isLoading = isLoading,
            onLogout = {
                viewModel.showConfirmationDialog(R.string.profile_logout_confirmation)
            },
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
}