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
import aragones.sergio.readercollection.presentation.ui.booklist.BookListView
import aragones.sergio.readercollection.presentation.ui.books.BooksView
import aragones.sergio.readercollection.presentation.ui.search.SearchView
import aragones.sergio.readercollection.presentation.ui.settings.SettingsView
import aragones.sergio.readercollection.presentation.ui.statistics.StatisticsView
import com.aragones.sergio.util.BookState

fun NavGraphBuilder.booksGraph(navController: NavHostController) {
    navigation<Route.Books>(startDestination = Route.BooksHome) {
        composable<Route.BooksHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            BooksView(
                onBookClick = { bookId ->
                },
                onShowAll = { bookState, sortParam, isSortDescending, query ->
                    navController.navigate(
                        Route.BookList(
                            state = bookState,
                            sortParam = sortParam,
                            isSortDescending = isSortDescending,
                            query = query,
                            year = -1,
                            month = -1,
                            author = null,
                            format = null,
                        ),
                    )
                },
                onAddBook = {
                    navController.navigate(Route.Search)
                },
            )
        }
        composable<Route.Search>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
            SearchView(
                onBookClick = { bookId ->
                },
                onBack = {
                    navController.navigateUp()
                },
            )
        }
        composable<Route.BookList>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
            BookListView(
                onBookClick = { bookId ->
                },
                onBack = {
                    navController.navigateUp()
                },
            )
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
            StatisticsView(
                onBookClick = { bookId ->
                },
                onShowAll = { sortParam, isSortDescending, year, month, author, format ->
                    navController.navigate(
                        Route.BookList(
                            state = BookState.READ,
                            sortParam = sortParam,
                            isSortDescending = isSortDescending,
                            query = "",
                            year = year,
                            month = month,
                            author = author,
                            format = format,
                        ),
                    )
                },
            )
        }
        composable<Route.BookList>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
            BookListView(
                onBookClick = { bookId ->
                },
                onBack = {
                    navController.navigateUp()
                },
            )
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
            SettingsView()
        }
    }
}