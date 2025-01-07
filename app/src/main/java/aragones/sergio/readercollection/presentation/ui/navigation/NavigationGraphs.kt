/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation

fun NavGraphBuilder.booksGraph(navController: NavHostController) {
    navigation<Route.Books>(startDestination = Route.BooksHome) {
        composable<Route.BooksHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
        }
        composable<Route.Search>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
        }
        composable<Route.BookList>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
        }
        composable<Route.BookDetail>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
        }
    }
}

fun NavGraphBuilder.statisticsGraph(navController: NavHostController) {
    navigation<Route.Statistics>(startDestination = Route.StatisticsHome) {
        composable<Route.StatisticsHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
        }
        composable<Route.BookList>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
        }
        composable<Route.BookDetail>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
        }
    }
}

fun NavGraphBuilder.settingsGraph() {
    navigation<Route.Settings>(startDestination = Route.SettingsHome) {
        composable<Route.SettingsHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
        }
    }
}