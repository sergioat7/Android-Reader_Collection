/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 30/6/2025
 */

package aragones.sergio.readercollection.presentation.account

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomOutlinedTextField
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import com.aragones.sergio.util.CustomInputType

@Composable
fun AccountScreen(
    state: AccountUiState,
    onShowInfo: () -> Unit,
    onProfileDataChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onChangePublicProfile: (Boolean) -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AccountToolbar(scrollState = scrollState, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(scrollState),
        ) {
            Spacer(Modifier.height(12.dp))
            HeaderText(text = stringResource(R.string.account_details_title))
            ProfileInfo(
                username = state.username,
                password = state.password,
                passwordError = state.passwordError,
                onShowInfo = onShowInfo,
                onPasswordChange = {
                    onProfileDataChange(it)
                },
            )
            MainActionButton(
                text = stringResource(R.string.save),
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp),
                enabled = state.passwordError == null,
                onClick = onSave,
            )
            Spacer(modifier = Modifier.height(24.dp))
            HeaderText(text = stringResource(R.string.account_management_title))
            PublicProfileItem(
                isEnabled = state.isProfilePublic,
                onChange = onChangePublicProfile,
            )
            Spacer(Modifier.height(12.dp))
            DeleteAccountItem(onClick = onDeleteAccount)
        }
    }
    if (state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun AccountToolbar(scrollState: ScrollState, onBack: (() -> Unit)) {
    val elevation = when (scrollState.value) {
        0 -> 0.dp
        else -> 4.dp
    }
    CustomToolbar(
        title = stringResource(R.string.account_title),
        modifier = Modifier.shadow(elevation = elevation),
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun HeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
    )
    Spacer(modifier = Modifier.height(16.dp))
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
        labelText = stringResource(R.string.username),
        onTextChanged = {},
        modifier = Modifier.fillMaxWidth(),
        endIcon = painterResource(R.drawable.ic_show_info)
            .withDescription(stringResource(R.string.show_info)),
        enabled = false,
        onEndIconClicked = onShowInfo,
    )
    Spacer(Modifier.height(8.dp))
    CustomOutlinedTextField(
        text = password,
        labelText = stringResource(R.string.password),
        onTextChanged = onPasswordChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        errorTextId = passwordError,
        endIcon = if (passwordVisibility) {
            painterResource(R.drawable.ic_hide_password)
                .withDescription(stringResource(R.string.hide_password))
        } else {
            painterResource(R.drawable.ic_show_password)
                .withDescription(stringResource(R.string.show_password))
        },
        inputType = CustomInputType.PASSWORD,
        isLastTextField = true,
        onEndIconClicked = { passwordVisibility = !passwordVisibility },
    )
}

@Composable
private fun PublicProfileItem(
    isEnabled: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.public_profile_title),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.public_profile_description),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isEnabled,
            onCheckedChange = onChange,
            thumbContent = {
                Icon(
                    imageVector = if (isEnabled) {
                        Icons.Default.Public
                    } else {
                        Icons.Default.PublicOff
                    },
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                )
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                checkedIconColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                uncheckedIconColor = MaterialTheme.colorScheme.secondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.secondary,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
            ),
        )
    }
}

@Composable
fun DeleteAccountItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.delete_account_title),
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.delete_account_description),
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = onClick,
            shape = MaterialTheme.shapes.small,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(
                text = stringResource(R.string.delete_account_action),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onError,
                maxLines = 2,
            )
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun AccountScreenPreview(
    @PreviewParameter(AccountScreenPreviewParameterProvider::class) state: AccountUiState,
) {
    ReaderCollectionTheme {
        AccountScreen(
            state = state,
            onShowInfo = {},
            onProfileDataChange = { _ -> },
            onBack = {},
            onSave = {},
            onChangePublicProfile = {},
            onDeleteAccount = {},
        )
    }
}

private class AccountScreenPreviewParameterProvider :
    PreviewParameterProvider<AccountUiState> {

    override val values: Sequence<AccountUiState>
        get() = sequenceOf(
            AccountUiState(
                username = "User",
                password = "Password",
                passwordError = null,
                isProfilePublic = true,
                isLoading = false,
            ),
            AccountUiState(
                username = "Username very very very very very very very long",
                password = "",
                passwordError = R.string.invalid_password,
                isProfilePublic = false,
                isLoading = false,
            ),
            AccountUiState(
                username = "User",
                password = "Password",
                passwordError = null,
                isProfilePublic = true,
                isLoading = true,
            ),
        )
}