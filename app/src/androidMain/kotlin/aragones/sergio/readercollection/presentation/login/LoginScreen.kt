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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.CustomInputType
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.create_account
import reader_collection.app.generated.resources.hide_password
import reader_collection.app.generated.resources.invalid_password
import reader_collection.app.generated.resources.invalid_username
import reader_collection.app.generated.resources.not_account
import reader_collection.app.generated.resources.password
import reader_collection.app.generated.resources.show_password
import reader_collection.app.generated.resources.sign_in
import reader_collection.app.generated.resources.username

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
            labelText = stringResource(Res.string.username),
            onTextChanged = { newUsername ->
                onLoginDataChange(newUsername, state.password)
            },
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorText = state.formState.usernameError?.let { stringResource(it) },
            isLastTextField = false,
            isRequired = true,
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = state.password,
            labelText = stringResource(Res.string.password),
            onTextChanged = { newPassword ->
                onLoginDataChange(state.username, newPassword)
            },
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorText = state.formState.passwordError?.let { stringResource(it) },
            endIcon = if (passwordVisibility) {
                painterResource(R.drawable.ic_hide_password)
                    .withDescription(stringResource(Res.string.hide_password))
            } else {
                painterResource(R.drawable.ic_show_password)
                    .withDescription(stringResource(Res.string.show_password))
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = true,
            isRequired = true,
            onEndIconClicked = { passwordVisibility = !passwordVisibility },
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(Res.string.sign_in),
            modifier = Modifier
                .widthIn(min = 200.dp)
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
                text = stringResource(Res.string.not_account),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(Modifier.width(5.dp))
            TextButton(onClick = onGoToRegister) {
                Text(
                    text = stringResource(Res.string.create_account),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@CustomPreviewLightDark
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
                    usernameError = Res.string.invalid_username,
                    passwordError = Res.string.invalid_password,
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