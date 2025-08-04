/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAddAlt1
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.domain.model.User
import aragones.sergio.readercollection.presentation.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.ListButton
import aragones.sergio.readercollection.presentation.components.MainActionButton
import aragones.sergio.readercollection.presentation.components.withDescription
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme

@Composable
fun FriendsScreen(
    state: FriendsUiState,
    onBack: () -> Unit,
    onSelectFriend: (String) -> Unit,
    onAcceptFriend: (String) -> Unit,
    onRejectFriend: (String) -> Unit,
    onDeleteFriend: (String) -> Unit,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        FriendsScreenToolbar(onBack = onBack)
        when (state) {
            FriendsUiState.Loading -> {
                CustomCircularProgressIndicator()
            }
            is FriendsUiState.Success -> {
                if (state.friends.isEmpty()) {
                    NoFriendsContent(onAddFriend)
                } else {
                    FriendsScreenContent(
                        state = state,
                        onSelectFriend = onSelectFriend,
                        onAcceptFriend = onAcceptFriend,
                        onRejectFriend = onRejectFriend,
                        onDeleteFriend = onDeleteFriend,
                        onAddFriend = onAddFriend,
                    )
                }
            }
        }
    }
}

@Composable
private fun FriendsScreenToolbar(onBack: (() -> Unit)) {
    CustomToolbar(
        title = stringResource(R.string.friends_title),
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun NoFriendsContent(onAddFriend: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.image_no_friends),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_friends_yet_title),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.no_friends_yet_subtitle),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        MainActionButton(
            text = stringResource(R.string.find_friends_action),
            enabled = true,
            onClick = onAddFriend,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(24.dp),
        )
    }
}

@Composable
private fun FriendsScreenContent(
    state: FriendsUiState.Success,
    onSelectFriend: (String) -> Unit,
    onAcceptFriend: (String) -> Unit,
    onRejectFriend: (String) -> Unit,
    onDeleteFriend: (String) -> Unit,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        ) {
            items(state.friends, key = { it.id }) { friend ->
                FriendItem(
                    friend = friend,
                    onSelectFriend = { onSelectFriend(friend.id) },
                    onAcceptFriend = { onAcceptFriend(friend.id) },
                    onRejectFriend = { onRejectFriend(friend.id) },
                    onDeleteFriend = { onDeleteFriend(friend.id) },
                )
            }
        }
        ListButton(
            painter = rememberVectorPainter(Icons.Default.PersonAddAlt1)
                .withDescription(stringResource(R.string.go_to_add_new_friend)),
            onClick = onAddFriend,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        )
    }
}

@Composable
private fun FriendItem(
    friend: User,
    onSelectFriend: () -> Unit,
    onAcceptFriend: () -> Unit,
    onRejectFriend: () -> Unit,
    onDeleteFriend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.clickable {
                if (friend.status == RequestStatus.APPROVED) {
                    onSelectFriend()
                }
            },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant,
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = friend.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 24.sp,
                )
                friend.status.title()?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = friend.status.color(),
                        lineHeight = 24.sp,
                    )
                }
            }
            if (friend.status == RequestStatus.REJECTED) {
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onDeleteFriend,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.delete),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        maxLines = 2,
                    )
                }
            }
        }
        if (friend.status == RequestStatus.PENDING_MINE) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = onAcceptFriend,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.add_friend_action),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1,
                    )
                }
                Button(
                    onClick = onRejectFriend,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(
                        text = stringResource(R.string.reject_friend_action),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onError,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestStatus.title(): String? = when (this) {
    RequestStatus.PENDING_MINE, RequestStatus.PENDING_FRIEND -> stringResource(
        R.string.pending_status,
    )
    RequestStatus.APPROVED -> null
    RequestStatus.REJECTED -> stringResource(R.string.rejected_status)
}

@Composable
private fun RequestStatus.color(): Color = when (this) {
    RequestStatus.PENDING_MINE, RequestStatus.PENDING_FRIEND -> MaterialTheme.colorScheme.tertiary
    RequestStatus.APPROVED -> Color.Unspecified
    RequestStatus.REJECTED -> MaterialTheme.colorScheme.error
}

@CustomPreviewLightDark
@Composable
private fun FriendsScreenPreview(
    @PreviewParameter(FriendsScreenPreviewParameterProvider::class) state: FriendsUiState,
) {
    ReaderCollectionTheme {
        FriendsScreen(
            state = state,
            onBack = {},
            onSelectFriend = {},
            onAcceptFriend = {},
            onRejectFriend = {},
            onDeleteFriend = {},
            onAddFriend = {},
        )
    }
}

private class FriendsScreenPreviewParameterProvider : PreviewParameterProvider<FriendsUiState> {

    override val values: Sequence<FriendsUiState>
        get() = sequenceOf(
            FriendsUiState.Success(
                friends = listOf(
                    User(
                        id = "1",
                        username = "User 1",
                        status = RequestStatus.APPROVED,
                    ),
                    User(
                        id = "2",
                        username = "user with a long name",
                        status = RequestStatus.APPROVED,
                    ),
                    User(
                        id = "3",
                        username =
                        """
                        User with a very long name
                        that will have to be fitted in two lines
                        """.trimIndent(),
                        status = RequestStatus.PENDING_MINE,
                    ),
                    User(
                        id = "4",
                        username = "User",
                        status = RequestStatus.PENDING_FRIEND,
                    ),
                    User(
                        id = "5",
                        username = "User",
                        status = RequestStatus.REJECTED,
                    ),
                ),
            ),
            FriendsUiState.Success(
                friends = emptyList(),
            ),
            FriendsUiState.Loading,
        )
}