/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 4/4/2024
 */

package aragones.sergio.readercollection.presentation.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.CustomRadioButton
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.description
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
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues()),
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
                        state.themeMode,
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
                        state.themeMode,
                    )
                },
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
                        state.themeMode,
                    )
                },
                onSortOrderValueChange = {
                    onProfileDataChange(
                        state.password,
                        state.language,
                        state.sortParam,
                        it,
                        state.themeMode,
                    )
                },
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
                },
            )
            Spacer(modifier = Modifier.weight(1f))
            MainActionButton(
                text = stringResource(R.string.save),
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
    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(R.string.title_settings),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
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
        labelText = stringResource(R.string.username),
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
        labelText = stringResource(R.string.password),
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
private fun LanguageInfo(language: String, onLanguageChange: (String) -> Unit) {
    HeaderText(
        text = stringResource(R.string.app_language),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
    ) {
        CustomRadioButton(
            text = stringResource(R.string.english),
            selected = language == Preferences.ENGLISH_LANGUAGE_KEY,
            onClick = {
                onLanguageChange(Preferences.ENGLISH_LANGUAGE_KEY)
            },
            modifier = Modifier.weight(1f),
        )
        CustomRadioButton(
            text = stringResource(R.string.spanish),
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
    val sortingParamValues = stringArrayResource(R.array.sorting_param_values).toList()
    val sortingParamKeys = stringArrayResource(R.array.sorting_param_keys).toList()
    val sortParamValue =
        if (sortParam == null) {
            sortingParamValues.first()
        } else {
            sortingParamValues[sortingParamKeys.indexOf(sortParam)]
        }
    val sortingOrderValues = stringArrayResource(R.array.sorting_order_values).toList()

    HeaderText(
        text = stringResource(R.string.sort_books_param),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    CustomDropdownMenu(
        currentValue = sortParamValue,
        values = sortingParamValues,
        onOptionSelected = {
            val index = sortingParamValues.indexOf(it)
            val newSortParam = sortingParamKeys[index].takeIf { index != 0 }
            onSortParamValueChange(newSortParam)
        },
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(8.dp))
    CustomDropdownMenu(
        currentValue = if (isSortDescending) {
            sortingOrderValues.last()
        } else {
            sortingOrderValues.first()
        },
        values = sortingOrderValues,
        onOptionSelected = {
            val index = sortingOrderValues.indexOf(it)
            onSortOrderValueChange(index == 1)
        },
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@Composable
private fun AppThemeInfo(selectedThemeIndex: Int, onThemeChange: (Int) -> Unit) {
    val appThemes = stringArrayResource(R.array.app_theme_values).toList()
    HeaderText(
        text = stringResource(R.string.app_theme),
        modifier = Modifier.padding(horizontal = 24.dp),
    )
    Spacer(Modifier.height(5.dp))
    CustomDropdownMenu(
        currentValue = appThemes[selectedThemeIndex],
        values = appThemes,
        onOptionSelected = {
            onThemeChange(appThemes.indexOf(it))
        },
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.body1,
        color = MaterialTheme.colorScheme.description,
    )
}

@PreviewLightDark
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(SettingsScreenPreviewParameterProvider::class) state: SettingsUiState,
) {
    ReaderCollectionTheme {
        SettingsScreen(
            state = state,
            onShowInfo = {},
            onProfileDataChange = { _, _, _, _, _ -> },
            onDeleteProfile = {},
            onLogout = {},
            onSave = {},
        )
    }
}

private class SettingsScreenPreviewParameterProvider :
    PreviewParameterProvider<SettingsUiState> {

    override val values: Sequence<SettingsUiState>
        get() = sequenceOf(
            SettingsUiState(
                username = "User",
                password = "Password",
                passwordError = null,
                language = "en",
                sortParam = null,
                isSortDescending = false,
                themeMode = 0,
                isLoading = false,
            ),
            SettingsUiState(
                username = "Username very very very very very very very long",
                password = "",
                passwordError = R.string.invalid_password,
                language = "es",
                sortParam = "pageCount",
                isSortDescending = true,
                themeMode = 1,
                isLoading = false,
            ),
            SettingsUiState(
                username = "User",
                password = "Password",
                passwordError = null,
                language = "en",
                sortParam = null,
                isSortDescending = false,
                themeMode = 0,
                isLoading = true,
            ),
        )
}