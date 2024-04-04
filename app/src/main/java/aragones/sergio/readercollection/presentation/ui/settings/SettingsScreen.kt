/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.ui.components.CustomRadioButton
import aragones.sergio.readercollection.presentation.ui.components.MainActionButton
import aragones.sergio.readercollection.presentation.ui.components.robotoSerifFamily

@Preview(device = Devices.NEXUS_5)
@Composable
fun SettingsScreen() {

    var passwordVisibility by rememberSaveable { mutableStateOf(false) }

    val padding12 = dimensionResource(id = R.dimen.padding_12dp).value
    val padding24 = dimensionResource(id = R.dimen.padding_24dp).value
    val margin20 = dimensionResource(id = R.dimen.margin_20dp).value
    val margin8 = dimensionResource(id = R.dimen.margin_8dp).value
    val margin5 = dimensionResource(id = R.dimen.margin_5dp).value
    val size200 = dimensionResource(id = R.dimen.size_200dp).value

    Column(
        Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.colorSecondary))
            .padding(padding24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CustomOutlinedTextField(
            text = "username",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = padding12.dp, end = padding12.dp, top = padding24.dp),
            labelText = stringResource(id = R.string.username),
            endIcon = R.drawable.ic_show_info,
            enabled = false,
            onTextChanged = {},
            onEndIconClicked = {
                //TODO: show dialog
            }
        )
        CustomOutlinedTextField(
            text = "password",
//            errorTextId = loginFormState.passwordError,
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
                //TODO: set password
            },
            onEndIconClicked = { passwordVisibility = !passwordVisibility }
        )
        HeaderText(
            text = stringResource(id = R.string.app_language),
            modifier = Modifier.padding(top = margin20.dp, bottom = margin5.dp)
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            CustomRadioButton(
                text = stringResource(id = R.string.english),
                modifier = Modifier.weight(1f),
                selected = true
            ) {
                //TODO:
            }
            CustomRadioButton(
                text = stringResource(id = R.string.spanish),
                modifier = Modifier.weight(1f),
                selected = false
            ) {
                //TODO:
            }
        }
        HeaderText(
            text = stringResource(id = R.string.sort_books_param),
            modifier = Modifier.padding(top = margin20.dp, bottom = margin5.dp)
        )
        CustomDropdownMenu(
            text = "Título",//TODO:
            modifier = Modifier.padding(bottom = margin8.dp),
            values = stringArrayResource(id = R.array.sorting_param_values).toList(),
            onOptionSelected = {
                //TODO:
            }
        )
        CustomDropdownMenu(
            text = "Ascendente",//TODO:
            modifier = Modifier,
            values = stringArrayResource(id = R.array.sorting_order_values).toList(),
            onOptionSelected = {
                //TODO:
            }
        )
        HeaderText(
            text = stringResource(id = R.string.app_theme),
            modifier = Modifier.padding(top = margin20.dp, bottom = margin5.dp)
        )
        CustomDropdownMenu(
            text = "Tema",//TODO:
            modifier = Modifier,
            values = stringArrayResource(id = R.array.app_theme_values).toList(),
            onOptionSelected = {
                //TODO:
            }
        )
        Spacer(modifier = Modifier.weight(1f))
        MainActionButton(
            text = stringResource(id = R.string.save),
            modifier = Modifier
                .width(size200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = padding12.dp, vertical = padding24.dp),
            enabled = true// loginFormState.isDataValid
        ) {
            //TODO: implement
        }
    }
}

@Composable
fun HeaderText(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = colorResource(id = R.color.textSecondary),
            fontFamily = robotoSerifFamily,
            fontWeight = FontWeight.Normal,
            fontSize = dimensionResource(id = R.dimen.text_size_16sp).value.sp
        )
    )
}