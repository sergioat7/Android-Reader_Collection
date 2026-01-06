/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/3/2024
 */

package aragones.sergio.readercollection.presentation.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.CustomInputType

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onShowInfo: () -> Unit,
    onRegisterDataChange: (String, String, String) -> Unit,
    onRegister: (String, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisibility by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.login_register_image),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(5f),
        )
        Spacer(Modifier.height(24.dp))
        CustomOutlinedTextField(
            text = state.username,
            labelText = stringResource(R.string.username),
            onTextChanged = { newUsername ->
                onRegisterDataChange(
                    newUsername,
                    state.password,
                    state.confirmPassword,
                )
            },
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorText = state.formState.usernameError?.let { stringResource(it) },
            endIcon = painterResource(R.drawable.ic_show_info)
                .withDescription(stringResource(R.string.show_info)),
            isLastTextField = false,
            isRequired = true,
            onEndIconClicked = onShowInfo,
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = state.password,
            labelText = stringResource(R.string.password),
            onTextChanged = { newPassword ->
                onRegisterDataChange(
                    state.username,
                    newPassword,
                    state.confirmPassword,
                )
            },
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorText = state.formState.passwordError?.let { stringResource(it) },
            endIcon = if (passwordVisibility) {
                painterResource(R.drawable.ic_hide_password)
                    .withDescription(stringResource(R.string.hide_password))
            } else {
                painterResource(R.drawable.ic_show_password)
                    .withDescription(stringResource(R.string.show_password))
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = false,
            isRequired = true,
            onEndIconClicked = { passwordVisibility = !passwordVisibility },
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = state.confirmPassword,
            labelText = stringResource(R.string.confirm_password),
            onTextChanged = { newPassword ->
                onRegisterDataChange(
                    state.username,
                    state.password,
                    newPassword,
                )
            },
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorText = state.formState.passwordError?.let { stringResource(it) },
            endIcon = if (confirmPasswordVisibility) {
                painterResource(R.drawable.ic_hide_password)
                    .withDescription(stringResource(R.string.hide_password))
            } else {
                painterResource(R.drawable.ic_show_password)
                    .withDescription(stringResource(R.string.show_password))
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = true,
            isRequired = true,
            onEndIconClicked = { confirmPasswordVisibility = !confirmPasswordVisibility },
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(R.string.sign_up),
            modifier = Modifier
                .widthIn(min = 200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            enabled = state.formState.isDataValid,
            onClick = {
                onRegister(state.username, state.password)
            },
        )
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@CustomPreviewLightDark
@Composable
private fun RegisterScreenPreview(
    @PreviewParameter(RegisterScreenPreviewParameterProvider::class) state: RegisterUiState,
) {
    ReaderCollectionTheme {
        RegisterScreen(
            state = state,
            onShowInfo = {},
            onRegisterDataChange = { _, _, _ -> },
            onRegister = { _, _ -> },
        )
    }
}

private class RegisterScreenPreviewParameterProvider :
    PreviewParameterProvider<RegisterUiState> {

    override val values: Sequence<RegisterUiState>
        get() = sequenceOf(
            RegisterUiState(
                username = "User",
                password = "Password",
                confirmPassword = "Password",
                formState = LoginFormState(
                    usernameError = null,
                    passwordError = null,
                    isDataValid = true,
                ),
                isLoading = false,
            ),
            RegisterUiState(
                username = "",
                password = "Password123",
                confirmPassword = "",
                formState = LoginFormState(
                    usernameError = R.string.invalid_username,
                    passwordError = R.string.invalid_repeat_password,
                    isDataValid = false,
                ),
                isLoading = false,
            ),
            RegisterUiState(
                username = "User",
                password = "Password",
                confirmPassword = "Password",
                formState = LoginFormState(
                    usernameError = null,
                    passwordError = null,
                    isDataValid = true,
                ),
                isLoading = true,
            ),
        )
}