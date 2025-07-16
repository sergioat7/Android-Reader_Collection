/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 11/1/2025
 */

package aragones.sergio.readercollection.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Route {

    // Graph routes

    @Serializable
    data object Auth : Route()

    @Serializable
    data object Books : Route()

    @Serializable
    data object Statistics : Route()

    @Serializable
    data object Settings : Route()

    // Screen routes

    @Serializable
    data object Login : Route()

    @Serializable
    data object Register : Route()

    @Serializable
    data object BooksHome : Route()

    @Serializable
    data object StatisticsHome : Route()

    @Serializable
    data object SettingsHome : Route()

    @Serializable
    data object Search : Route()

    @Serializable
    data class BookDetail(val bookId: String) : Route()

    @Serializable
    data class BookList(
        val state: String,
        val sortParam: String?,
        val isSortDescending: Boolean,
        val query: String,
        val year: Int = -1,
        val month: Int = -1,
        val author: String? = null,
        val format: String? = null,
    ) : Route()

    @Serializable
    data object Account : Route()

    @Serializable
    data object Friends : Route()

    @Serializable
    data object DataSync : Route()

    @Serializable
    data object DisplaySettings : Route()
}