/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2025
 */

package aragones.sergio.readercollection.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.presentation.navigation.booksGraph
import aragones.sergio.readercollection.presentation.navigation.settingsGraph
import aragones.sergio.readercollection.presentation.navigation.statisticsGraph
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.roseBud

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val showBottomNavigationBar = navBackStackEntry?.destination?.route?.contains(
                "home",
                true,
            ) ?: false
            if (showBottomNavigationBar) {
                BottomNavigationBar(navController, navBackStackEntry)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            NavigationStack(navController)
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?,
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.secondary,
    ) {
        val currentDestination = navBackStackEntry?.destination
        for (item in NavItem.entries) {
            val selected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
            val title = stringResource(item.title)
            BottomNavigationItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(painter = painterResource(item.icon), contentDescription = title)
                },
                label = {
                    Text(
                        text = title,
                        style = if (selected) {
                            MaterialTheme.typography.h3
                        } else {
                            MaterialTheme.typography.body2
                        },
                        color = MaterialTheme.colors.secondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                selectedContentColor = MaterialTheme.colors.roseBud,
                unselectedContentColor = MaterialTheme.colors.secondary,
            )
        }
    }
}

@Composable
private fun NavigationStack(navController: NavHostController) {
    val navGraph = remember(navController) {
        navController.createGraph(startDestination = Route.Books) {
            booksGraph(navController)
            statisticsGraph(navController)
            settingsGraph()
        }
    }
    NavHost(
        navController = navController,
        graph = navGraph,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    )
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    ReaderCollectionTheme {
        MainScreen()
    }
}

private enum class NavItem(
    val route: Route,
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
) {
    BOOKS(Route.Books, R.drawable.ic_bookshelf, R.string.title_books),
    STATISTICS(Route.Statistics, R.drawable.ic_book_statistics, R.string.title_stats),
    SETTINGS(Route.Settings, R.drawable.ic_settings, R.string.title_settings),
}