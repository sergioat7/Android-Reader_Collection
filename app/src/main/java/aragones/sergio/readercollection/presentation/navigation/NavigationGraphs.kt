/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2025
 */

package aragones.sergio.readercollection.presentation.navigation

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.MainActivity
import aragones.sergio.readercollection.presentation.account.AccountView
import aragones.sergio.readercollection.presentation.bookdetail.BookDetailView
import aragones.sergio.readercollection.presentation.booklist.BookListView
import aragones.sergio.readercollection.presentation.books.BooksView
import aragones.sergio.readercollection.presentation.landing.LandingActivity
import aragones.sergio.readercollection.presentation.login.LoginView
import aragones.sergio.readercollection.presentation.register.RegisterView
import aragones.sergio.readercollection.presentation.search.SearchView
import aragones.sergio.readercollection.presentation.settings.SettingsOption
import aragones.sergio.readercollection.presentation.settings.SettingsView
import aragones.sergio.readercollection.presentation.statistics.StatisticsView
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
                    val options = ActivityOptions
                        .makeCustomAnimation(
                            context,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                        ).toBundle()
                    context.startActivity(intent, options)
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
                    val options = ActivityOptions
                        .makeCustomAnimation(
                            context,
                            R.anim.slide_in_right,
                            R.anim.slide_out_left,
                        ).toBundle()
                    context.startActivity(intent, options)
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
            BooksView(
                onBookClick = { bookId ->
                    navController.navigate(
                        Route.BookDetail(bookId),
                    )
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
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { slideOutOfContainer() },
        ) {
            SearchView(
                onBookClick = { bookId ->
                    navController.navigate(
                        Route.BookDetail(bookId),
                    )
                },
                onBack = {
                    navController.navigateUp()
                },
            )
        }
        composable<Route.BookList>(
            enterTransition = { slideIntoContainer() },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { slideOutOfContainer() },
        ) {
            BookListView(
                onBookClick = { bookId ->
                    navController.navigate(
                        Route.BookDetail(bookId),
                    )
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
            BookDetailView(
                onBack = {
                    navController.navigateUp()
                },
            )
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
                    navController.navigate(
                        Route.BookDetail(bookId),
                    )
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
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { slideOutOfContainer() },
        ) {
            BookListView(
                onBookClick = { bookId ->
                    navController.navigate(
                        Route.BookDetail(bookId),
                    )
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
            BookDetailView(
                onBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}

fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    navigation<Route.Settings>(startDestination = Route.SettingsHome) {
        composable<Route.SettingsHome>(
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            val context = LocalContext.current
            SettingsView(
                onClickOption = {
                    when (it) {
                        is SettingsOption.Account -> navController.navigate(Route.Account)
                        is SettingsOption.DataSync -> navController.navigate(Route.DataSync)
                        is SettingsOption.DisplaySettings -> navController.navigate(
                            Route.DisplaySettings,
                        )
                        is SettingsOption.Logout -> logout(context)
                    }
                },
            )
        }
        composable<Route.Account>(
            enterTransition = { slideIntoContainer() },
            exitTransition = { slideOutOfContainer() },
        ) {
            val context = LocalContext.current
            AccountView(
                onBack = {
                    navController.navigateUp()
                },
                onLogout = {
                    logout(context)
                },
            )
        }
    }
}

private fun logout(context: Context) {
    val intent = Intent(context, LandingActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra("SKIP_ANIMATION", true)
    }
    val options = ActivityOptions
        .makeCustomAnimation(
            context,
            R.anim.slide_in_left,
            R.anim.slide_out_right,
        ).toBundle()
    context.startActivity(intent, options)
}