/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2026
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
expect fun StatisticsView(
    onBookClick: (String) -> Unit,
    onShowAll: (String?, Boolean, Int, Int, String?, String?) -> Unit,
    viewModel: StatisticsViewModel = koinViewModel(),
)