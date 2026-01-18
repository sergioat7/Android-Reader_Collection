/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.displaysettings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DisplaySettingsView(
    onBack: () -> Unit,
    onRelaunch: () -> Unit,
    viewModel: DisplaySettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state

    val relaunch by viewModel.relaunch.collectAsState()
    if (relaunch) {
        onRelaunch()
        return
    }

    ReaderCollectionApp {
        DisplaySettingsScreen(
            state = state,
            onBack = onBack,
            onProfileDataChange = viewModel::profileDataChanged,
            onSave = viewModel::save,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.onResume()
    }
}