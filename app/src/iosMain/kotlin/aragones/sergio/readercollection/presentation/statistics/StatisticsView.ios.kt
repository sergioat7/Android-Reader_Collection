/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2026
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import aragones.sergio.readercollection.presentation.components.LaunchedEffectOnce
import aragones.sergio.readercollection.presentation.theme.ReaderCollectionApp

@Composable
actual fun StatisticsView(
    onBookClick: (String) -> Unit,
    onShowAll: (String?, Boolean, Int, Int, String?, String?) -> Unit,
    viewModel: StatisticsViewModel,
) {
    val state by viewModel.state.collectAsState()

    ReaderCollectionApp(navigationBarSameAsBackground = false) {
        StatisticsScreen(
            state = state,
            onImportClick = {},
            onExportClick = {},
            onGroupClick = { year, month, author, format ->
                onShowAll(
                    viewModel.sortParam,
                    viewModel.isSortDescending,
                    year ?: -1,
                    month ?: -1,
                    author,
                    format,
                )
            },
            onBookClick = onBookClick,
        )
    }

    LaunchedEffectOnce {
        viewModel.fetchBooks()
    }
}