/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.ConfirmationAlertDialog
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.user_remove_confirmation

@Composable
fun FriendDetailView(
    onBack: () -> Unit,
    onBookClick: (String, String) -> Unit,
    viewModel: FriendDetailViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val confirmationMessageId by viewModel.confirmationDialogMessageId.collectAsState()
    val infoDialogMessageId by viewModel.infoDialogMessageId.collectAsState()
    val error by viewModel.error.collectAsState()

    ReaderCollectionApp {
        FriendDetailScreen(
            state = state,
            onBack = onBack,
            onBookClick = onBookClick,
            onDeleteFriend = {
                viewModel.showConfirmationDialog(Res.string.user_remove_confirmation)
            },
        )
    }

    ConfirmationAlertDialog(
        textId = confirmationMessageId,
        onCancel = {
            viewModel.closeDialogs()
        },
        onAccept = {
            viewModel.closeDialogs()
            viewModel.deleteFriend()
        },
    )

    val text = if (error != null) {
        val errorText = StringBuilder()
        if (requireNotNull(error).error.isNotEmpty()) {
            errorText.append(requireNotNull(error).error)
        } else {
            errorText.append(stringResource(requireNotNull(error).errorKey))
        }
        errorText.toString()
    } else if (infoDialogMessageId != null) {
        stringResource(requireNotNull(infoDialogMessageId))
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
        onBack()
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFriend()
    }
}