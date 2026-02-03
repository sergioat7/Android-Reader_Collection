/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2026
 */

package aragones.sergio.readercollection.presentation.landing

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.presentation.navigation.authGraph
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
fun LandingView(
    navigator: Navigator,
    viewModel: LandingViewModel,
    skipAnimation: Boolean = false,
    isAppUpdated: Boolean = false,
) {
    val animationFinished = rememberSaveable { mutableStateOf(false) }
    ReaderCollectionApp {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(WindowInsets.safeDrawing.asPaddingValues()),
        ) {
            val navController = rememberNavController()
            val isLogged = viewModel.isLogged.collectAsState()
            when (isLogged.value) {
                true -> {
                    navigator.goToMain()
                }
                false -> {
                    val navGraph = remember(navController) {
                        navController.createGraph(startDestination = Route.Auth) {
                            authGraph(navController, navigator)
                        }
                    }
                    NavHost(
                        navController = navController,
                        graph = navGraph,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    )
                }
                else -> {
                    if (skipAnimation) {
                        animationFinished.value = true
                    } else {
                        LandingScreen(onAnimationFinished = {
                            animationFinished.value = true
                        })
                    }
                }
            }
        }

        LaunchedEffect(isAppUpdated, animationFinished.value) {
            if (isAppUpdated && animationFinished.value) {
                viewModel.checkIsLoggedIn()
            }
        }
        LaunchedEffect(Unit) {
            viewModel.mapGenres()
        }
    }
}