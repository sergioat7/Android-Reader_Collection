/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.displaysettings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomDropdownMenu
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomRadioButton
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.Preferences

@Composable
fun DisplaySettingsScreen(
    state: DisplaySettingsUiState,
    onBack: () -> Unit,
    onProfileDataChange: (String, String?, Boolean, Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        DisplaySettingsToolbar(scrollState = scrollState, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(12.dp))
            HeaderText(text = stringResource(R.string.app_language))
            LanguageInfo(
                language = state.language,
                onLanguageChange = {
                    onProfileDataChange(
                        it,
                        state.sortParam,
                        state.isSortDescending,
                        state.themeMode,
                    )
                },
            )
            Spacer(Modifier.height(24.dp))
            HeaderText(text = stringResource(R.string.sort_books_param))
            SortingInfo(
                sortParam = state.sortParam,
                isSortDescending = state.isSortDescending,
                onSortParamValueChange = {
                    onProfileDataChange(
                        state.language,
                        it,
                        state.isSortDescending,
                        state.themeMode,
                    )
                },
                onSortOrderValueChange = {
                    onProfileDataChange(
                        state.language,
                        state.sortParam,
                        it,
                        state.themeMode,
                    )
                },
            )
            Spacer(Modifier.height(24.dp))
            HeaderText(text = stringResource(R.string.app_theme))
            AppThemeInfo(
                selectedThemeIndex = state.themeMode,
                onThemeChange = {
                    onProfileDataChange(
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
                    .widthIn(min = 200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp),
                enabled = true,
                onClick = onSave,
            )
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun DisplaySettingsToolbar(scrollState: ScrollState, onBack: (() -> Unit)) {
    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(R.string.display_settings_title),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.semantics { heading() },
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun LanguageInfo(language: String, onLanguageChange: (String) -> Unit) {
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

    CustomDropdownMenu(
        currentValue = sortParamValue,
        values = sortingParamValues,
        labelText = stringResource(R.string.sort_param),
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
        labelText = stringResource(R.string.sort_order),
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
    CustomDropdownMenu(
        currentValue = appThemes[selectedThemeIndex],
        values = appThemes,
        labelText = stringResource(R.string.app_theme),
        onOptionSelected = {
            onThemeChange(appThemes.indexOf(it))
        },
        modifier = Modifier.padding(horizontal = 24.dp),
    )
}

@CustomPreviewLightDark
@Composable
private fun AccountScreenPreview(
    @PreviewParameter(DisplaySettingsScreenPreviewParameterProvider::class) state:
    DisplaySettingsUiState,
) {
    ReaderCollectionTheme {
        DisplaySettingsScreen(
            state = state,
            onBack = {},
            onProfileDataChange = { _, _, _, _ -> },
            onSave = {},
        )
    }
}

private class DisplaySettingsScreenPreviewParameterProvider :
    PreviewParameterProvider<DisplaySettingsUiState> {

    override val values: Sequence<DisplaySettingsUiState>
        get() = sequenceOf(
            DisplaySettingsUiState(
                language = "en",
                sortParam = null,
                isSortDescending = false,
                themeMode = 0,
                isLoading = false,
            ),
            DisplaySettingsUiState(
                language = "es",
                sortParam = "pageCount",
                isSortDescending = true,
                themeMode = 1,
                isLoading = false,
            ),
            DisplaySettingsUiState(
                language = "en",
                sortParam = null,
                isSortDescending = false,
                themeMode = 0,
                isLoading = true,
            ),
        )
}