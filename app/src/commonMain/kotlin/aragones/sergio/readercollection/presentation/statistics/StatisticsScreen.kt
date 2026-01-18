/*
 * Copyright (c) 2026 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 15/1/2026
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun StatisticsScreen(
    state: StatisticsUiState,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
)