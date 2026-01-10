/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.InformationAlertDialog
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AddFriendsView(onBack: () -> Unit, viewModel: AddFriendsViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsState()
    val error by viewModel.error.collectAsState()

    ReaderCollectionApp {
        AddFriendsScreen(
            state = state,
            onBack = onBack,
            onSearch = viewModel::searchUserWith,
            onRequestFriend = viewModel::requestFriendship,
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
    } else {
        ""
    }
    InformationAlertDialog(show = text.isNotEmpty(), text = text) {
        viewModel.closeDialogs()
    }
}