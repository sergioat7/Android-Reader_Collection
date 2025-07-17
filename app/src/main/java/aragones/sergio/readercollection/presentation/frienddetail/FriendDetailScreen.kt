/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.presentation.components.ButtonType
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@Composable
fun FriendDetailScreen(
    state: FriendDetailUiState,
    onBack: () -> Unit,
    onDeleteFriend: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        FriendDetailScreenToolbar(onBack = onBack)
        when (state) {
            FriendDetailUiState.Loading -> {
                CustomCircularProgressIndicator()
            }
            is FriendDetailUiState.Success -> {
                FriendDetailContent(
                    friend = state.friend,
                    onDeleteFriend = { onDeleteFriend(state.friend.id) },
                )
            }
        }
    }
}

@Composable
private fun FriendDetailScreenToolbar(onBack: (() -> Unit)) {
    CustomToolbar(
        title = "",
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun FriendDetailContent(
    friend: User,
    onDeleteFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        UserInfo(user = friend)
        Spacer(Modifier.weight(1f))
        MainActionButton(
            text = stringResource(R.string.delete),
            modifier = Modifier
                .widthIn(min = 200.dp)
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 24.dp),
            enabled = true,
            onClick = onDeleteFriend,
            type = ButtonType.DESTRUCTIVE,
        )
    }
}

@Composable
private fun UserInfo(user: User) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(150.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(Modifier.height(24.dp))
    Text(
        text = user.username,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@CustomPreviewLightDark
@Composable
private fun FriendDetailScreenPreview(
    @PreviewParameter(FriendDetailScreenPreviewParameterProvider::class) state: FriendDetailUiState,
) {
    ReaderCollectionTheme {
        FriendDetailScreen(
            state = state,
            onBack = {},
            onDeleteFriend = {},
        )
    }
}

private class FriendDetailScreenPreviewParameterProvider :
    PreviewParameterProvider<FriendDetailUiState> {

    override val values: Sequence<FriendDetailUiState>
        get() = sequenceOf(
            FriendDetailUiState.Success(
                friend = User(
                    id = "1",
                    username = "User 1",
                    status = RequestStatus.APPROVED,
                ),
            ),
            FriendDetailUiState.Loading,
        )
}