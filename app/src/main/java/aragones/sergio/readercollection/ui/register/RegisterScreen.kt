/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 24/3/2024
 */

package aragones.sergio.readercollection.ui.register

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.ui.components.MainActionButton

@Preview
@Composable
fun RegisterScreenPreview() {
    RegisterScreen()
}

@Composable
fun RegisterScreen() {

    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    val padding12 = dimensionResource(id = R.dimen.padding_12dp).value
    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    val margin8 = dimensionResource(id = R.dimen.margin_8dp).value
    val size200 = dimensionResource(id = R.dimen.size_200dp).value

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
        )
        CustomOutlinedTextField(
            text = "username",
            errorTextId = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = padding24.dp),
            labelText = stringResource(id = R.string.username),
            endIcon = R.drawable.ic_show_info,
            onTextChanged = { newUsername ->
                //TODO:
            },
            onEndIconClicked = {
                //TODO:
            }
        )
        CustomOutlinedTextField(
            text = "password",
            errorTextId = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = margin8.dp),
            labelText = stringResource(id = R.string.password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            onTextChanged = { newPassword ->
                //TODO:
            },
            onEndIconClicked = { passwordVisibility = !passwordVisibility }
        )
        CustomOutlinedTextField(
            text = "confirm_password",
            errorTextId = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = margin8.dp),
            labelText = stringResource(id = R.string.confirm_password),
            endIcon = if (passwordVisibility) {
                R.drawable.ic_hide_password
            } else {
                R.drawable.ic_show_password
            },
            onTextChanged = { newPassword ->
                //TODO:
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
            enabled = true
        ) {
            //TODO:
        }
    }
}