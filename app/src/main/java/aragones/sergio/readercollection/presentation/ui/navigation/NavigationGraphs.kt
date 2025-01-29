/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.navigation

import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import aragones.sergio.readercollection.presentation.ui.MainActivity
import aragones.sergio.readercollection.presentation.ui.bookdetail.BookDetailActivity
import aragones.sergio.readercollection.presentation.ui.booklist.BookListView
import aragones.sergio.readercollection.presentation.ui.books.BooksView
import aragones.sergio.readercollection.presentation.ui.landing.LandingActivity
import aragones.sergio.readercollection.presentation.ui.login.LoginView
import aragones.sergio.readercollection.presentation.ui.register.RegisterView
import aragones.sergio.readercollection.presentation.ui.search.SearchView
import aragones.sergio.readercollection.presentation.ui.settings.SettingsView
import aragones.sergio.readercollection.presentation.ui.statistics.StatisticsView
import com.aragones.sergio.util.BookState

fun NavGraphBuilder.authGraph(navController: NavHostController) {
    navigation<Route.Auth>(startDestination = Route.Login) {
        composable<Route.Login>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            val context = LocalContext.current
            LoginView(
                onGoToMain = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                onGoToRegister = {
                    navController.navigate(Route.Register)
                },
            )
        }
        composable<Route.Register>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
            val context = LocalContext.current
            RegisterView(
                onGoToMain = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
            )
        }
    }
}

fun NavGraphBuilder.booksGraph(navController: NavHostController) {
    navigation<Route.Books>(startDestination = Route.BooksHome) {
        composable<Route.BooksHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            val context = LocalContext.current
            BooksView(
                onBookClick = { bookId ->
                    val intent = Intent(context, BookDetailActivity::class.java).apply {
                        putExtra("bookId", bookId)
                        putExtra("isGoogleBook", false)
                    }
                    context.startActivity(intent)
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
            val context = LocalContext.current
            SearchView(
                onBookClick = { bookId ->
                    val intent = Intent(context, BookDetailActivity::class.java).apply {
                        putExtra("bookId", bookId)
                        putExtra("isGoogleBook", true)
                    }
                    context.startActivity(intent)
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
            val context = LocalContext.current
            BookListView(
                onBookClick = { bookId ->
                    val intent = Intent(context, BookDetailActivity::class.java).apply {
                        putExtra("bookId", bookId)
                        putExtra("isGoogleBook", false)
                    }
                    context.startActivity(intent)
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
            val context = LocalContext.current
            StatisticsView(
                onBookClick = { bookId ->
                    val intent = Intent(context, BookDetailActivity::class.java).apply {
                        putExtra("bookId", bookId)
                        putExtra("isGoogleBook", false)
                    }
                    context.startActivity(intent)
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
            val context = LocalContext.current
            BookListView(
                onBookClick = { bookId ->
                    val intent = Intent(context, BookDetailActivity::class.java).apply {
                        putExtra("bookId", bookId)
                        putExtra("isGoogleBook", false)
                    }
                    context.startActivity(intent)
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
            val context = LocalContext.current
            SettingsView(
                onRelaunch = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
                onLogout = {
                    val intent = Intent(context, LandingActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    context.startActivity(intent)
                },
            )
        }
    }
}