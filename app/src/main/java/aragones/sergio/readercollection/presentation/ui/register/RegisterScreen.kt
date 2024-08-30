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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.ui.components.MainActionButton
import aragones.sergio.readercollection.presentation.ui.login.model.LoginFormState

@Composable
fun RegisterScreen(viewModel: RegisterViewModel) {

    val username by viewModel.username.observeAsState(initial = "")
    val password by viewModel.password.observeAsState(initial = "")
    val confirmPassword by viewModel.confirmPassword.observeAsState(initial = "")
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisibility by rememberSaveable { mutableStateOf(false) }
    val registerFormState by viewModel.registerFormState.observeAsState(initial = LoginFormState())
    val loading by viewModel.registerLoading.observeAsState(initial = false)

    val buttonEnabled by remember {
        derivedStateOf { registerFormState.isDataValid }
    }

    val padding12 = dimensionResource(id = R.dimen.padding_12dp).value
    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    val margin8 = dimensionResource(id = R.dimen.margin_8dp).value
    val size200 = dimensionResource(id = R.dimen.size_200dp).value

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(padding24.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_register_image),
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(5f),
        )
        CustomOutlinedTextField(
            text = username,
            errorTextId = registerFormState.usernameError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = padding24.dp),
            labelText = stringResource(id = R.string.username),
            endIcon = R.drawable.ic_show_info,
            isLastTextField = false,
            onTextChanged = { newUsername ->
                viewModel.registerDataChanged(newUsername, password, confirmPassword)
            },
            onEndIconClicked = {
                viewModel.showInfoDialog(R.string.username_info)
            },
        )
        CustomOutlinedTextField(
            text = password,
            errorTextId = registerFormState.passwordError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = margin8.dp),
            labelText = stringResource(id = R.string.password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            isLastTextField = false,
            onTextChanged = { newPassword ->
                viewModel.registerDataChanged(username, newPassword, confirmPassword)
            },
            onEndIconClicked = { passwordVisibility = !passwordVisibility },
        )
        CustomOutlinedTextField(
            text = confirmPassword,
            errorTextId = registerFormState.passwordError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = margin8.dp),
            labelText = stringResource(id = R.string.confirm_password),
            endIcon = if (confirmPasswordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            isLastTextField = true,
            onTextChanged = { newPassword ->
                viewModel.registerDataChanged(username, password, newPassword)
            },
            onEndIconClicked = { confirmPasswordVisibility = !confirmPasswordVisibility },
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(id = R.string.sign_up),
            modifier = Modifier
                .width(size200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = padding12.dp, vertical = padding24.dp),
            enabled = buttonEnabled,
        ) {
            viewModel.register(username, password)
        }
    }

    if (loading) {
        CustomCircularProgressIndicator()
    }
}