/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/7/2025
 */

package aragones.sergio.readercollection.presentation.addfriend

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun AddFriendsView(onBack: () -> Unit, viewModel: AddFriendsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    ReaderCollectionApp {
        AddFriendsScreen(
            state = state,
            onBack = onBack,
            onSearch = {},
            onRequestFriend = {},
        )
    }
}