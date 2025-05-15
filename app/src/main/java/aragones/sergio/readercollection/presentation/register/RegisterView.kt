/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2025
 */

package aragones.sergio.readercollection.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog

@Composable
fun RegisterView(onGoToMain: () -> Unit, viewModel: RegisterViewModel = hiltViewModel()) {
    val state by viewModel.uiState
    val error by viewModel.registerError.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    val activityName = viewModel.activityName.collectAsState()
    when (activityName.value) {
        MainActivity::class.simpleName -> onGoToMain()
        else -> Unit
    }

    RegisterScreen(
        state = state,
        onShowInfo = { viewModel.showInfoDialog(R.string.username_info) },
        onRegisterDataChange = viewModel::registerDataChanged,
        onRegister = viewModel::register,
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
}