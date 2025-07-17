/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 16/7/2025
 */

package aragones.sergio.readercollection.presentation.frienddetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun FriendDetailView(onBack: () -> Unit, viewModel: FriendDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    ReaderCollectionApp {
        FriendDetailScreen(
            state = state,
            onBack = onBack,
            onDeleteFriend = {},
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFriend()
    }
}