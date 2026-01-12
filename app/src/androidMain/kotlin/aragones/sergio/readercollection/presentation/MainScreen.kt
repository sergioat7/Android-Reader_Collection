/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 7/1/2025
 */

package aragones.sergio.readercollection.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.createGraph
import aragones.sergio.readercollection.presentation.components.CustomPreviewLightDark
import aragones.sergio.readercollection.presentation.navigation.Navigator
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.presentation.navigation.booksGraph
import aragones.sergio.readercollection.presentation.navigation.settingsGraph
import aragones.sergio.readercollection.presentation.navigation.statisticsGraph
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionTheme
import aragones.sergio.readercollection.presentation.theme.roseBud
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import reader_collection.app.generated.resources.Res
import reader_collection.app.generated.resources.ic_book_statistics
import reader_collection.app.generated.resources.ic_bookshelf
import reader_collection.app.generated.resources.ic_settings
import reader_collection.app.generated.resources.title_books
import reader_collection.app.generated.resources.title_settings
import reader_collection.app.generated.resources.title_stats

@Composable
fun MainScreen(navigator: Navigator, snackbarHostState: SnackbarHostState = SnackbarHostState()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        },
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
        Box(modifier = Modifier.padding(bottom = padding.calculateBottomPadding())) {
            NavigationStack(navController, navigator)
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    navBackStackEntry: NavBackStackEntry?,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.secondary,
    ) {
        val currentDestination = navBackStackEntry?.destination
        for (item in NavItem.entries) {
            val selected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
            val title = stringResource(item.title)
            NavigationBarItem(
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
                            MaterialTheme.typography.displaySmall
                        } else {
                            MaterialTheme.typography.bodyMedium
                        },
                        color = MaterialTheme.colorScheme.secondary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.roseBud,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                ),
            )
        }
    }
}

@Composable
private fun NavigationStack(navController: NavHostController, navigator: Navigator) {
    val navGraph = remember(navController) {
        navController.createGraph(startDestination = Route.Books) {
            booksGraph(navController)
            statisticsGraph(navController)
            settingsGraph(navController, navigator)
        }
    }
    NavHost(
        navController = navController,
        graph = navGraph,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    )
}

@CustomPreviewLightDark
@Composable
private fun MainScreenPreview() {
    ReaderCollectionTheme {
        MainScreen(object : Navigator {
            override fun goToLanding() {}
            override fun goToMain(withOptions: Boolean) {}
        })
    }
}

private enum class NavItem(
    val route: Route,
    val icon: DrawableResource,
    val title: StringResource,
) {
    BOOKS(Route.Books, Res.drawable.ic_bookshelf, Res.string.title_books),
    STATISTICS(Route.Statistics, Res.drawable.ic_book_statistics, Res.string.title_stats),
    SETTINGS(Route.Settings, Res.drawable.ic_settings, Res.string.title_settings),
}