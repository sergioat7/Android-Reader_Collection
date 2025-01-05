/*
 * Copyright (c) 2025 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 5/1/2025
 */

package aragones.sergio.readercollection.presentation.ui.statistics

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.components.CustomCircularProgressIndicator
import aragones.sergio.readercollection.presentation.ui.components.CustomToolbar
import aragones.sergio.readercollection.presentation.ui.components.NoResultsComponent
import aragones.sergio.readercollection.presentation.ui.components.TopAppBarIcon
import aragones.sergio.readercollection.presentation.ui.theme.ReaderCollectionTheme
import com.aragones.sergio.util.BookState
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry

@Composable
fun StatisticsScreen(
    state: StatisticsUiState,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
    ) {
        StatisticsToolbar(
            state = state,
            scrollState = scrollState,
            onImportClick = onImportClick,
            onExportClick = onExportClick,
        )
        StatisticsContent(
            state = state,
            scrollState = scrollState,
            onGroupClick = onGroupClick,
            onBookClick = onBookClick,
            modifier = Modifier.padding(12.dp),
        )
    }
    if (state is StatisticsUiState.Success && state.isLoading) {
        CustomCircularProgressIndicator()
    }
}

@Composable
private fun StatisticsToolbar(
    state: StatisticsUiState,
    scrollState: ScrollState,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val booksRead = when (state) {
        is StatisticsUiState.Empty -> 0
        is StatisticsUiState.Success -> state.totalBooksRead
    }

    CustomToolbar(
        title = stringResource(R.string.title_stats),
        modifier = modifier.background(MaterialTheme.colors.background),
        elevation = when (scrollState.value) {
            0 -> 0.dp
            else -> 4.dp
        },
        subtitle = pluralStringResource(R.plurals.title_books_count, booksRead, booksRead),
        actions = {
            TopAppBarIcon(
                icon = R.drawable.ic_file_import,
                onClick = onImportClick,
            )
            TopAppBarIcon(
                icon = R.drawable.ic_file_export,
                onClick = onExportClick,
            )
        },
    )
}

@Composable
private fun StatisticsContent(
    state: StatisticsUiState,
    scrollState: ScrollState,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(Modifier.height(16.dp))
        when (state) {
            is StatisticsUiState.Empty -> NoResultsComponent()
            is StatisticsUiState.Success -> StatisticsComponent(
                state = state,
                onGroupClick = onGroupClick,
                onBookClick = onBookClick,
            )
        }
    }
}

@Composable
private fun StatisticsComponent(
    state: StatisticsUiState.Success,
    onGroupClick: (Int?, Int?, String?, String?) -> Unit,
    onBookClick: (String) -> Unit,
) {
    if (state.booksByYearEntries.isNotEmpty()) {
        BooksByYear(
            entries = state.booksByYearEntries,
            onYearSelected = { onGroupClick(it, null, null, null) },
        )
    }
    if (state.booksByMonthEntries.isNotEmpty()) {
        BooksByMonth(
            entries = state.booksByMonthEntries,
            onMonthSelected = { onGroupClick(null, it, null, null) },
        )
    }
    if (state.booksByAuthorStats.isNotEmpty()) {
        BooksByAuthor(
            entries = state.booksByAuthorStats,
            onAuthorSelected = { onGroupClick(null, null, it, null) },
        )
    }
    BooksByPages(
        shorterBook = state.shorterBook,
        longerBook = state.longerBook,
        onBookClick = onBookClick,
    )
    if (state.booksByFormatEntries.isNotEmpty()) {
        BooksByFormat(
            entries = state.booksByFormatEntries,
            onFormatSelected = { onGroupClick(null, null, null, it) },
        )
    }
}

@Composable
private fun BooksByYear(entries: List<BarEntry>, onYearSelected: (Int?) -> Unit) {

}

@Composable
private fun BooksByMonth(entries: List<PieEntry>, onMonthSelected: (Int?) -> Unit) {

}

@Composable
private fun BooksByAuthor(entries: Map<String, List<Book>>, onAuthorSelected: (String?) -> Unit) {

}

@Composable
private fun BooksByPages(shorterBook: Book?, longerBook: Book?, onBookClick: (String) -> Unit) {

}

@Composable
private fun BooksByFormat(entries: List<PieEntry>, onFormatSelected: (String?) -> Unit) {

}

@PreviewLightDark
@Composable
private fun StatisticsScreenPreview(
    @PreviewParameter(StatisticsScreenPreviewParameterProvider::class) state: StatisticsUiState,
) {
    ReaderCollectionTheme {
        StatisticsScreen(
            state = state,
            onImportClick = {},
            onExportClick = {},
            onGroupClick = { _, _, _, _ -> },
            onBookClick = {},
        )
    }
}

private class StatisticsScreenPreviewParameterProvider :
    PreviewParameterProvider<StatisticsUiState> {

    private val book = Book(
        "1",
        "Shortest read book",
        null,
        listOf("Author"),
        null,
        null,
        null,
        null,
        null,
        null,
        0,
        null,
        0.0,
        0,
        5.0,
        null,
        null,
        null,
        BookState.READ,
        false,
        0,
    )

    override val values: Sequence<StatisticsUiState>
        get() = sequenceOf(
            StatisticsUiState.Success(
                totalBooksRead = 12345,
                booksByYearEntries = listOf(),
                booksByMonthEntries = listOf(),
                booksByAuthorStats = mapOf(),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = listOf(),
                isLoading = false,
            ),
            StatisticsUiState.Success(
                totalBooksRead = 12345,
                booksByYearEntries = listOf(),
                booksByMonthEntries = listOf(),
                booksByAuthorStats = mapOf(),
                shorterBook = book.copy(title = "Shortest read book"),
                longerBook = book.copy(title = "Longest read book"),
                booksByFormatEntries = listOf(),
                isLoading = true,
            ),
            StatisticsUiState.Empty,
        )
}
