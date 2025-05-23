/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2025
 */

package aragones.sergio.readercollection.presentation.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog

@Composable
fun LoginView(
    onGoToMain: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState
    val error by viewModel.loginError.collectAsState()

    val activityName = viewModel.activityName.collectAsState()
    when (activityName.value) {
        MainActivity::class.simpleName -> onGoToMain()
        else -> Unit
    }

    LoginScreen(
        state = state,
        onLoginDataChange = viewModel::loginDataChanged,
        onLogin = viewModel::login,
        onGoToRegister = onGoToRegister,
    )

    val errorText = StringBuilder()
    error?.let {
        if (it.error.isNotEmpty()) {
            errorText.append(it.error)
        } else {
            errorText.append(stringResource(it.errorKey))
        }
    }
    InformationAlertDialog(show = error != null, text = errorText.toString()) {
        viewModel.closeDialogs()
    }
}