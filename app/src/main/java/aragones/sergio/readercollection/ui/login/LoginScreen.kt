/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 20/3/2024
 */

package aragones.sergio.readercollection.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.ui.components.MainActionButton
import aragones.sergio.readercollection.ui.components.robotoSerifFamily
import aragones.sergio.readercollection.ui.login.model.LoginFormState

@Composable
fun LoginScreen(viewModel: LoginViewModel) {

    val username by viewModel.username.observeAsState(initial = "")
    val password by viewModel.password.observeAsState(initial = "")
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    val loginFormState by viewModel.loginFormState.observeAsState(initial = LoginFormState())
    val loading by viewModel.loginLoading.observeAsState(initial = false)

    val padding12 = dimensionResource(id = R.dimen.padding_12dp).value
    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    val margin8 = dimensionResource(id = R.dimen.margin_8dp).value
    val size200 = dimensionResource(id = R.dimen.size_200dp).value

    val textSize16 = dimensionResource(id = R.dimen.text_size_16sp).value

    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSecondary))
            .padding(padding24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.login_register_image),
            contentDescription = "",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .weight(5f)
        )
        CustomOutlinedTextField(
            text = username,
            errorTextId = loginFormState.usernameError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = padding24.dp),
            labelText = stringResource(id = R.string.username),
            isLastTextField = false,
            onTextChanged = { newUsername ->
                viewModel.loginDataChanged(newUsername, password)
            }
        )
        CustomOutlinedTextField(
            text = password,
            errorTextId = loginFormState.passwordError,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = margin8.dp),
            labelText = stringResource(id = R.string.password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            isLastTextField = true,
            onTextChanged = { newPassword ->
                viewModel.loginDataChanged(username, newPassword)
            },
            onEndIconClicked = { passwordVisibility = !passwordVisibility }
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(id = R.string.sign_in),
            modifier = Modifier
                .width(size200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = padding12.dp, vertical = padding24.dp),
            enabled = loginFormState.isDataValid
        ) {
            viewModel.login(username, password)
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = stringResource(id = R.string.not_account),
                modifier = Modifier.padding(end = 5.dp),
                style = TextStyle(
                    color = colorResource(id = R.color.textSecondary),
                    fontFamily = robotoSerifFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = textSize16.sp
                )
            )
            TextButton(onClick = { viewModel.goToRegister() }) {
                Text(
                    text = stringResource(id = R.string.create_account),
                    style = TextStyle(
                        color = colorResource(id = R.color.textPrimary),
                        fontFamily = robotoSerifFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = textSize16.sp,
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }

    if (loading) {
        CustomCircularProgressIndicator()
    }
}