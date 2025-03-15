/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2025
 */

package aragones.sergio.readercollection.presentation.statistics

import aragones.sergio.readercollection.domain.model.Book
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry

sealed class StatisticsUiState {

    data object Empty : StatisticsUiState()

    data class Success(
        val totalBooksRead: Int,
        val booksByYearEntries: List<BarEntry>,
        val booksByMonthEntries: List<PieEntry>,
        val booksByAuthorStats: Map<String, List<Book>>,
        val shorterBook: Book?,
        val longerBook: Book?,
        val booksByFormatEntries: List<PieEntry>,
        val isLoading: Boolean,
    ) : StatisticsUiState() {
        companion object {
            fun empty(): Success = Success(
                totalBooksRead = 0,
                booksByYearEntries = listOf(),
                booksByMonthEntries = listOf(),
                booksByAuthorStats = mapOf(),
                shorterBook = null,
                longerBook = null,
                booksByFormatEntries = listOf(),
                isLoading = false,
            )
        }
    }
}