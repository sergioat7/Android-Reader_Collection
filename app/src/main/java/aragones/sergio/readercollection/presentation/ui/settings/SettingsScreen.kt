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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.ui.components.CustomRadioButton
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.MainActionButton
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.theme.description
import com.aragones.sergio.util.Preferences

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {

    val password by viewModel.password.observeAsState(initial = "")
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    val passwordError by viewModel.profileForm.observeAsState(initial = null)
    val language by viewModel.language.observeAsState(initial = "")
    val sortParam by viewModel.sortParam.observeAsState(initial = null)
    val isSortDescending by viewModel.isSortDescending.observeAsState(initial = false)
    val themeMode by viewModel.themeMode.observeAsState(initial = 0)
    val loading by viewModel.profileLoading.observeAsState(initial = false)
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        CustomToolbar(
            title = stringResource(id = R.string.title_settings),
            modifier = Modifier.background(MaterialTheme.colors.background),
            elevation = when (scrollState.value) {
                0 -> 0.dp
                else -> 4.dp
            },
            actions = {
                TopAppBarIcon(
                    icon = R.drawable.ic_delete_profile,
                    onClick = { viewModel.showConfirmationDialog(R.string.profile_delete_confirmation) },
                )
                TopAppBarIcon(
                    icon = R.drawable.ic_logout,
                    onClick = { viewModel.showConfirmationDialog(R.string.profile_logout_confirmation) },
                )
            }
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {

            CustomOutlinedTextField(
                text = viewModel.username,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp),
                labelText = stringResource(id = R.string.username),
                endIcon = R.drawable.ic_show_info,
                enabled = false,
                onTextChanged = {},
                onEndIconClicked = {
                    viewModel.showInfoDialog(R.string.username_info)
                },
            )
            CustomOutlinedTextField(
                text = password,
                errorTextId = passwordError,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp),
                labelText = stringResource(id = R.string.password),
                endIcon = if (passwordVisibility) {
                    R.drawable.ic_hide_password
                } else {
                    R.drawable.ic_show_password
                },
                isLastTextField = true,
                onTextChanged = {

                    viewModel.profileDataChanged(
                        it,
                        language,
                        sortParam,
                        isSortDescending,
                        themeMode
                    )
                },
                onEndIconClicked = { passwordVisibility = !passwordVisibility },
            )
            HeaderText(
                text = stringResource(id = R.string.app_language),
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 5.dp,
                    start = 24.dp,
                    end = 24.dp,
                ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                CustomRadioButton(
                    text = stringResource(id = R.string.english),
                    modifier = Modifier.weight(1f),
                    selected = language == Preferences.ENGLISH_LANGUAGE_KEY,
                ) {

                    viewModel.profileDataChanged(
                        password,
                        Preferences.ENGLISH_LANGUAGE_KEY,
                        sortParam,
                        isSortDescending,
                        themeMode
                    )
                }
                CustomRadioButton(
                    text = stringResource(id = R.string.spanish),
                    modifier = Modifier.weight(1f),
                    selected = language == Preferences.SPANISH_LANGUAGE_KEY,
                ) {

                    viewModel.profileDataChanged(
                        password,
                        Preferences.SPANISH_LANGUAGE_KEY,
                        sortParam,
                        isSortDescending,
                        themeMode
                    )
                }
            }
            HeaderText(
                text = stringResource(id = R.string.sort_books_param),
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 5.dp,
                    start = 24.dp,
                    end = 24.dp,
                ),
            )
            val sortingParamValues = stringArrayResource(id = R.array.sorting_param_values).toList()
            val sortingParamKeys = stringArrayResource(id = R.array.sorting_param_keys).toList()
            val sortParamValue =
                if (sortParam == null) sortingParamValues.first()
                else sortingParamValues[sortingParamKeys.indexOf(sortParam)]
            CustomDropdownMenu(
                currentValue = sortParamValue,
                modifier = Modifier.padding(
                    bottom = 8.dp,
                    start = 24.dp,
                    end = 24.dp,
                ),
                values = sortingParamValues,
                onOptionSelected = {

                    val index = sortingParamValues.indexOf(it)
                    val newSortParam =
                        if (index == 0) null else sortingParamKeys[index]
                    viewModel.profileDataChanged(
                        password,
                        language,
                        newSortParam,
                        isSortDescending,
                        themeMode
                    )
                },
            )
            val sortingOrderValues = stringArrayResource(id = R.array.sorting_order_values).toList()
            CustomDropdownMenu(
                currentValue = if (isSortDescending) sortingOrderValues.last() else sortingOrderValues.first(),
                modifier = Modifier.padding(horizontal = 24.dp),
                values = sortingOrderValues,
                onOptionSelected = {

                    val index = sortingOrderValues.indexOf(it)
                    viewModel.profileDataChanged(
                        password,
                        language,
                        sortParam,
                        index == 1,
                        themeMode
                    )
                },
            )
            HeaderText(
                text = stringResource(id = R.string.app_theme),
                modifier = Modifier.padding(
                    top = 20.dp,
                    bottom = 5.dp,
                    start = 24.dp,
                    end = 24.dp,
                ),
            )
            val appThemes = stringArrayResource(id = R.array.app_theme_values).toList()
            CustomDropdownMenu(
                currentValue = appThemes[themeMode],
                modifier = Modifier.padding(horizontal = 24.dp),
                values = appThemes,
                onOptionSelected = {

                    viewModel.profileDataChanged(
                        password,
                        language,
                        sortParam,
                        isSortDescending,
                        appThemes.indexOf(it)
                    )
                },
            )
            Spacer(modifier = Modifier.weight(1f))
            MainActionButton(
                text = stringResource(id = R.string.save),
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                enabled = passwordError == null,
            ) {
                viewModel.save()
            }
        }
    }

    if (loading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
fun HeaderText(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.description,
    )
}