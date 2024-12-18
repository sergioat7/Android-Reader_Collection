/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/3/2024
 */

package aragones.sergio.readercollection.presentation.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.ui.components.MainActionButton
import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.CustomInputType

@Composable
fun RegisterScreen(
    username: String,
    password: String,
    confirmPassword: String,
    formState: LoginFormState,
    isLoading: Boolean,
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
            text = username,
            onTextChanged = { newUsername ->
                onRegisterDataChange(newUsername, password, confirmPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorTextId = formState.usernameError,
            labelText = stringResource(id = R.string.username),
            endIcon = R.drawable.ic_show_info,
            isLastTextField = false,
            onEndIconClicked = onShowInfo,
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = password,
            onTextChanged = { newPassword ->
                onRegisterDataChange(username, newPassword, confirmPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorTextId = formState.passwordError,
            labelText = stringResource(id = R.string.password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = false,
            onEndIconClicked = { passwordVisibility = !passwordVisibility },
        )
        Spacer(Modifier.height(8.dp))
        CustomOutlinedTextField(
            text = confirmPassword,
            onTextChanged = { newPassword ->
                onRegisterDataChange(username, password, newPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            errorTextId = formState.passwordError,
            labelText = stringResource(id = R.string.confirm_password),
            endIcon = if (confirmPasswordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            inputType = CustomInputType.PASSWORD,
            isLastTextField = true,
            onEndIconClicked = { confirmPasswordVisibility = !confirmPasswordVisibility },
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(id = R.string.sign_up),
            modifier = Modifier
                .width(200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            enabled = formState.isDataValid,
            onClick = {
                onRegister(username, password)
            },
        )
    }
    if (isLoading) {
        CustomCircularProgressIndicator()
    }
}

@PreviewLightDark
@Composable
private fun RegisterScreenPreview() {
    ReaderCollectionTheme {
        RegisterScreen(
            username = "User",
            password = "Password123",
            confirmPassword = "",
            formState = LoginFormState(
                usernameError = null,
                passwordError = R.string.invalid_repeat_password,
                isDataValid = false,
            ),
            isLoading = false,
            onShowInfo = {},
            onRegisterDataChange = { _, _, _ -> },
            onRegister = { _, _ -> },
        )
    }
}