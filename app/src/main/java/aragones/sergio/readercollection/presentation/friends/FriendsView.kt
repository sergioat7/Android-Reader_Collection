/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun FriendsView(
    onBack: () -> Unit,
    onSelectFriend: (String) -> Unit,
    onAddFriend: () -> Unit,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val error by viewModel.error.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()

    ReaderCollectionApp {
        FriendsScreen(
            state = state,
            onBack = onBack,
            onSelectFriend = onSelectFriend,
            onAcceptFriend = viewModel::acceptFriendRequest,
            onRejectFriend = viewModel::rejectFriendRequest,
            onDeleteFriend = viewModel::deleteFriend,
            onAddFriend = onAddFriend,
        )
    }

    val text = if (error != null) {
        val errorText = StringBuilder()
        if (requireNotNull(error).error.isNotEmpty()) {
            errorText.append(requireNotNull(error).error)
        } else {
            errorText.append(stringResource(requireNotNull(error).errorKey))
        }
        errorText.toString()
    } else if (infoDialogMessageId != -1) {
        stringResource(infoDialogMessageId)
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }
}