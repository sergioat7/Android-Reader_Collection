/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2025
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.compose.runtime.Immutable
import aragones.sergio.readercollection.domain.model.Book
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry

sealed class StatisticsUiState {

    data object Empty : StatisticsUiState()

    data class Success(
        val totalBooksRead: Int,
        val booksByYearEntries: BarEntries,
        val booksByMonthEntries: PieEntries,
        val booksByAuthorStats: MapEntries,
        val shorterBook: Book?,
        val longerBook: Book?,
        val booksByFormatEntries: PieEntries,
        val isLoading: Boolean,
    ) : StatisticsUiState() {
        companion object {
            fun empty(): Success = Success(
                totalBooksRead = 0,
                booksByYearEntries = BarEntries(),
                booksByMonthEntries = PieEntries(),
                booksByAuthorStats = MapEntries(),
                shorterBook = null,
                longerBook = null,
                booksByFormatEntries = PieEntries(),
                isLoading = false,
            )
        }
    }
}

@Immutable
data class BarEntries(val entries: List<BarEntry> = emptyList())

@Immutable
data class PieEntries(val entries: List<PieEntry> = emptyList())

@Immutable
data class MapEntries(val entries: Map<String, List<Book>> = mapOf())