/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 12/7/2025
 */

package aragones.sergio.readercollection.presentation.friends

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun FriendsView(onBack: () -> Unit, viewModel: FriendsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    ReaderCollectionApp {
        FriendsScreen(
            state = state,
            onBack = onBack,
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }
}