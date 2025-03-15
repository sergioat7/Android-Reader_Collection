/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/3/2024
 */

package aragones.sergio.readercollection.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
import com.aragones.sergio.util.CustomInputType

@Composable
fun LoginScreen(
    state: LoginUiState,
    onLoginDataChange: (String, String) -> Unit,
    onLogin: (String, String) -> Unit,
    onGoToRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(24.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_register_image),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(5f),
        )
        Spacer(Modifier.height(24.dp))
        CustomOutlinedTextField(
            text = state.username,
            onTextChanged = { newUsername ->
                onLoginDataChange(newUsername, state.password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorTextId = state.formState.usernameError,
            labelText = stringResource(id = R.string.username),
            isLastTextField = false,
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = state.password,
            onTextChanged = { newPassword ->
                onLoginDataChange(state.username, newPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorTextId = state.formState.passwordError,
            labelText = stringResource(id = R.string.password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = true,
            onEndIconClicked = { passwordVisibility = !passwordVisibility },
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(id = R.string.sign_in),
            modifier = Modifier
                .width(200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            enabled = state.formState.isDataValid,
            onClick = {
                onLogin(state.username, state.password)
            },
        )
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(id = R.string.not_account),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.description,
            )
            Spacer(Modifier.width(5.dp))
            TextButton(onClick = onGoToRegister) {
                Text(
                    text = stringResource(id = R.string.create_account),
                    style = MaterialTheme.typography.body1.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = MaterialTheme.colors.primary,
                )
            }
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@PreviewLightDark
@Composable
private fun LoginScreenPreview(
    @PreviewParameter(LoginScreenPreviewParameterProvider::class) state: LoginUiState,
) {
    ReaderCollectionTheme {
        LoginScreen(
            state = state,
            onLoginDataChange = { _, _ -> },
            onLogin = { _, _ -> },
            onGoToRegister = {},
        )
    }
}

private class LoginScreenPreviewParameterProvider :
    PreviewParameterProvider<LoginUiState> {

    override val values: Sequence<LoginUiState>
        get() = sequenceOf(
            LoginUiState(
                username = "User",
                password = "Password",
                formState = LoginFormState(
                    usernameError = null,
                    passwordError = null,
                    isDataValid = true,
                ),
                isLoading = false,
            ),
            LoginUiState(
                username = "",
                password = "",
                formState = LoginFormState(
                    usernameError = R.string.invalid_username,
                    passwordError = R.string.invalid_password,
                    isDataValid = false,
                ),
                isLoading = false,
            ),
            LoginUiState(
                username = "User",
                password = "Password",
                formState = LoginFormState(
                    usernameError = null,
                    passwordError = null,
                    isDataValid = true,
                ),
                isLoading = true,
            ),
        )
}