/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import aragones.sergio.readercollection.data.remote.model.RequestStatus
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.components.CustomToolbar
import aragones.sergio.readercollection.presentation.components.SearchBar
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.add_friend_action
import reader_collection.app.generated.resources.add_friends_title
import reader_collection.app.generated.resources.no_friends_found
import reader_collection.app.generated.resources.search_friends_instructions

@Composable
fun AddFriendsScreen(
    state: AddFriendsUiState,
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    onRequestFriend: (UserUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AddFriendsToolbar(onBack = onBack)
        Column(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .align(Alignment.CenterHorizontally)
                .fillMaxSize(),
        ) {
            Spacer(Modifier.height(16.dp))
            SearchBar(
                text = state.query,
                onSearch = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                showLeadingIcon = true,
                requestFocusByDefault = false,
            )
            Spacer(Modifier.height(16.dp))
            when (state) {
                is AddFriendsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
                is AddFriendsUiState.Success -> {
                    Spacer(Modifier.height(16.dp))
                    if (state.users.users.isEmpty()) {
                        val textKey =
                            if (state.query.isEmpty()) {
                                Res.string.search_friends_instructions
                            } else {
                                Res.string.no_friends_found
                            }
                        Text(
                            text = stringResource(textKey),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 24.dp),
                        )
                    } else {
                        AddFriendsContent(
                            users = state.users,
                            onRequestFriend = onRequestFriend,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFriendsToolbar(onBack: (() -> Unit)) {
    CustomToolbar(
        title = stringResource(Res.string.add_friends_title),
        backgroundColor = MaterialTheme.colorScheme.background,
        onBack = onBack,
    )
}

@Composable
private fun AddFriendsContent(
    users: UsersUi,
    onRequestFriend: (UserUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
    ) {
        items(users.users, key = { it.id }) { friend ->
            FriendItem(
                friend = friend,
                onRequestFriend = { onRequestFriend(friend) },
            )
        }
    }
}

@Composable
private fun FriendItem(friend: UserUi, onRequestFriend: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
            when (friend.status) {
                RequestStatus.PENDING_FRIEND -> {
                    Spacer(modifier = Modifier.width(16.dp))
                    if (friend.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = onRequestFriend,
                            modifier = Modifier.widthIn(max = 320.dp),
                            shape = MaterialTheme.shapes.small,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Text(
                                text = stringResource(Res.string.add_friend_action),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 2,
                            )
                        }
                    }
                }
                RequestStatus.PENDING_MINE,
                RequestStatus.APPROVED,
                RequestStatus.REJECTED,
                -> {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Añadido",
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 24.sp,
                    )
                }
            }
        }
    }
}

@CustomPreviewLightDark
@Composable
private fun AddFriendsScreenPreview(
    @PreviewParameter(AddFriendsScreenPreviewParameterProvider::class) state: AddFriendsUiState,
) {
    ReaderCollectionTheme {
        AddFriendsScreen(
            state = state,
            onBack = {},
            onSearch = {},
            onRequestFriend = {},
        )
    }
}

private class AddFriendsScreenPreviewParameterProvider :
    PreviewParameterProvider<AddFriendsUiState> {

    override val values: Sequence<AddFriendsUiState>
        get() = sequenceOf(
            AddFriendsUiState.Success(
                users = UsersUi(
                    listOf(
                        UserUi(
                            id = "1",
                            username = "Username pending",
                            status = RequestStatus.PENDING_MINE,
                            isLoading = false,
                        ),
                        UserUi(
                            id = "2",
                            username = "username pending",
                            status = RequestStatus.PENDING_FRIEND,
                            isLoading = true,
                        ),
                        UserUi(
                            id = "3",
                            username = "username approved",
                            status = RequestStatus.APPROVED,
                            isLoading = false,
                        ),
                        UserUi(
                            id = "4",
                            username = "username rejected",
                            status = RequestStatus.REJECTED,
                            isLoading = false,
                        ),
                    ),
                ),
                query = "Username",
            ),
            AddFriendsUiState.Success(
                users = UsersUi(),
                query = "",
            ),
            AddFriendsUiState.Loading(""),
        )
}