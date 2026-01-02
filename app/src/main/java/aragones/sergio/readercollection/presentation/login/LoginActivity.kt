/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 19/10/2020
 */

package aragones.sergio.readercollection.presentation.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.presentation.navigation.authGraph
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

class LoginActivity : ComponentActivity() {

    //region Lifecycle methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ReaderCollectionApp {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(WindowInsets.safeDrawing.asPaddingValues()),
                ) {
                    val navController = rememberNavController()
                    val navGraph = remember(navController) {
                        navController.createGraph(startDestination = Route.Auth) {
                            authGraph(navController)
                        }
                    }
                    NavHost(
                        navController = navController,
                        graph = navGraph,
                        enterTransition = { EnterTransition.None },
                        exitTransition = { ExitTransition.None },
                    )
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            moveTaskToBack(true)
        }
    }
    //endregion
}