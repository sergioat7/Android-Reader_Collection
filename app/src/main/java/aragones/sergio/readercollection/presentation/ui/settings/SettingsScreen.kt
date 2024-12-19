/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.ui.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.ui.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.ui.components.CustomRadioButton
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.MainActionButton
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.ui.theme.description
import com.aragones.sergio.util.CustomInputType
import com.aragones.sergio.util.Preferences

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onShowInfo: () -> Unit,
    onProfileDataChange: (String, String, String?, Boolean, Int) -> Unit,
    onDeleteProfile: () -> Unit,
    onLogout: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        SettingsToolbar(
            scrollState = scrollState,
            onDeleteProfile = onDeleteProfile,
            onLogout = onLogout,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(24.dp))
            ProfileInfo(
                username = state.username,
                password = state.password,
                passwordError = state.passwordError,
                onShowInfo = onShowInfo,
                onPasswordChange = {
                    onProfileDataChange(
                        it,
                        state.language,
                        state.sortParam,
                        state.isSortDescending,
                        state.themeMode
                    )
                },
            )
            Spacer(Modifier.height(20.dp))
            LanguageInfo(
                language = state.language,
                onLanguageChange = {
                    onProfileDataChange(
                        state.password,
                        it,
                        state.sortParam,
                        state.isSortDescending,
                        state.themeMode
                    )
                }
            )
            Spacer(Modifier.height(20.dp))
            SortingInfo(
                sortParam = state.sortParam,
                isSortDescending = state.isSortDescending,
                onSortParamValueChange = {
                    onProfileDataChange(
                        state.password,
                        state.language,
                        it,
                        state.isSortDescending,
                        state.themeMode
                    )
                },
                onSortOrderValueChange = {
                    onProfileDataChange(
                        state.password,
                        state.language,
                        state.sortParam,
                        it,
                        state.themeMode
                    )
                }
            )
            Spacer(Modifier.height(20.dp))
            AppThemeInfo(
                selectedThemeIndex = state.themeMode,
                onThemeChange = {
                    onProfileDataChange(
                        state.password,
                        state.language,
                        state.sortParam,
                        state.isSortDescending,
                        it,
                    )
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            MainActionButton(
                text = stringResource(id = R.string.save),
                modifier = Modifier
                    .width(200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                enabled = state.passwordError == null,
                onClick = onSave,
            )
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun SettingsToolbar(
    scrollState: ScrollState,
    onDeleteProfile: () -> Unit,
    onLogout: () -> Unit,
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
                onClick = onDeleteProfile,
            )
            TopAppBarIcon(
                icon = R.drawable.ic_logout,
                onClick = onLogout,
            )
        },
    )
}

@Composable
private fun ProfileInfo(
    username: String,
    password: String,
    passwordError: Int?,
    onShowInfo: () -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    var passwordVisibility by rememberSaveable { mutableStateOf(false) }
    CustomOutlinedTextField(
        text = username,
        onTextChanged = {},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        labelText = stringResource(id = R.string.username),
        endIcon = R.drawable.ic_show_info,
        enabled = false,
        onEndIconClicked = onShowInfo,
    )
    Spacer(Modifier.height(8.dp))
    CustomOutlinedTextField(
        text = password,
        onTextChanged = onPasswordChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        errorTextId = passwordError,
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
}

@Composable
private fun LanguageInfo(
    language: String,
    onLanguageChange: (String) -> Unit,
) {
    HeaderText(
        text = stringResource(id = R.string.app_language),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        CustomRadioButton(
            text = stringResource(id = R.string.english),
            selected = language == Preferences.ENGLISH_LANGUAGE_KEY,
            onClick = {
                onLanguageChange(Preferences.ENGLISH_LANGUAGE_KEY)
            },
            modifier = Modifier.weight(1f),
        )
        CustomRadioButton(
            text = stringResource(id = R.string.spanish),
            selected = language == Preferences.SPANISH_LANGUAGE_KEY,
            onClick = {
                onLanguageChange(Preferences.SPANISH_LANGUAGE_KEY)
            },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SortingInfo(
    sortParam: String?,
    isSortDescending: Boolean,
    onSortParamValueChange: (String?) -> Unit,
    onSortOrderValueChange: (Boolean) -> Unit,
) {
    val sortingParamValues = stringArrayResource(id = R.array.sorting_param_values).toList()
    val sortingParamKeys = stringArrayResource(id = R.array.sorting_param_keys).toList()
    val sortParamValue =
        if (sortParam == null) sortingParamValues.first()
        else sortingParamValues[sortingParamKeys.indexOf(sortParam)]
    val sortingOrderValues = stringArrayResource(id = R.array.sorting_order_values).toList()

    HeaderText(
        text = stringResource(id = R.string.sort_books_param),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    CustomDropdownMenu(
        currentValue = sortParamValue,
        modifier = Modifier.padding(horizontal = 24.dp),
        values = sortingParamValues,
        onOptionSelected = {

            val index = sortingParamValues.indexOf(it)
            val newSortParam = sortingParamKeys[index].takeIf { index != 0 }
            onSortParamValueChange(newSortParam)
        },
    )
    Spacer(Modifier.height(8.dp))
    CustomDropdownMenu(
        currentValue = if (isSortDescending) sortingOrderValues.last() else sortingOrderValues.first(),
        modifier = Modifier.padding(horizontal = 24.dp),
        values = sortingOrderValues,
        onOptionSelected = {

            val index = sortingOrderValues.indexOf(it)
            onSortOrderValueChange(index == 1)
        },
    )
}

@Composable
private fun AppThemeInfo(
    selectedThemeIndex: Int,
    onThemeChange: (Int) -> Unit,
) {
    val appThemes = stringArrayResource(id = R.array.app_theme_values).toList()
    HeaderText(
        text = stringResource(id = R.string.app_theme),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    CustomDropdownMenu(
        currentValue = appThemes[selectedThemeIndex],
        modifier = Modifier.padding(horizontal = 24.dp),
        values = appThemes,
        onOptionSelected = {
            onThemeChange(appThemes.indexOf(it))
        },
    )
}

@Composable
private fun HeaderText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colors.description,
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview() {
    ReaderCollectionTheme {
        SettingsScreen(
            state = SettingsUiState(
                username = "User",
                password = "Password",
                passwordError = null,
                language = "en",
                sortParam = null,
                isSortDescending = false,
                themeMode = 0,
                isLoading = false,
            ),
            onShowInfo = {},
            onProfileDataChange = { _, _, _, _, _ -> },
            onDeleteProfile = {},
            onLogout = {},
            onSave = {},
        )
    }
}