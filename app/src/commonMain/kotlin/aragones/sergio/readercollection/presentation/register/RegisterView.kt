/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2025
 */

package aragones.sergio.readercollection.presentation.register

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.username_info

@Composable
fun RegisterView(onGoToMain: () -> Unit, viewModel: RegisterViewModel = koinViewModel()) {
    val state by viewModel.uiState
    val error by viewModel.registerError.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    val registerSuccess = viewModel.registerSuccess.collectAsState()
    when (registerSuccess.value) {
        true -> onGoToMain()
        false -> Unit
    }

    RegisterScreen(
        state = state,
        onShowInfo = { viewModel.showInfoDialog(Res.string.username_info) },
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
    } else if (infoDialogMessageId != null) {
        stringResource(requireNotNull(infoDialogMessageId))
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }
}