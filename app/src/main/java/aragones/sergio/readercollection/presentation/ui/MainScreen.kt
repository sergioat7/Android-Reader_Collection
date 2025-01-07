/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2025
 */

package aragones.sergio.readercollection.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
        }
    }
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    ReaderCollectionTheme {
        MainScreen()
    }
}