/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.UiDateMapper.getGroupedBy
import com.aragones.sergio.util.extensions.toString
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.number

class StatisticsViewModel(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var _state: MutableStateFlow<StatisticsUiState> =
        MutableStateFlow(StatisticsUiState.Empty)
    private val _booksError = MutableStateFlow<ErrorModel?>(null)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: StateFlow<StatisticsUiState> = _state
    val booksError: StateFlow<ErrorModel?> = _booksError
    var sortParam = userRepository.sortParam
    var isSortDescending = userRepository.isSortDescending
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Public methods
    fun fetchBooks() = viewModelScope.launch {
        _state.update {
            when (it) {
                StatisticsUiState.Empty -> StatisticsUiState.Success.empty().copy(isLoading = true)
                is StatisticsUiState.Success -> it.copy(isLoading = true)
            }
        }

        booksRepository.getReadBooks().collect { books ->
            _state.value = when (books.isEmpty()) {
                true -> StatisticsUiState.Empty
                false -> StatisticsUiState.Success(
                    totalBooksRead = books.size,
                    booksByYearEntries = createBooksByYearStats(books),
                    booksByMonthEntries = createBooksByMonthStats(books),
                    booksByAuthorStats = createBooksByAuthorStats(books),
                    shorterBook = books
                        .filter { it.pageCount > 0 }
                        .minByOrNull { it.pageCount },
                    longerBook = books
                        .filter { it.pageCount > 0 }
                        .maxByOrNull { it.pageCount },
                    booksByFormatEntries = createFormatStats(books),
                    isLoading = false,
                )
            }
        }
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _booksError.value = null
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
    }

    fun importData(jsonData: String) = viewModelScope.launch {
        booksRepository.importDataFrom(jsonData).fold(
            onSuccess = {
                _infoDialogMessageId.value = R.string.data_imported
            },
            onFailure = {
                _booksError.value = ErrorModel("", R.string.error_file_data)
            },
        )
    }

    fun getDataToExport(completion: (String?) -> Unit) = viewModelScope.launch {
        booksRepository.exportDataTo().fold(
            onSuccess = {
                completion(it)
                _infoDialogMessageId.value = R.string.file_created
            },
            onFailure = {
                completion(null)
                _booksError.value = ErrorModel("", R.string.error_database)
            },
        )
    }
    //endregion

    //region Private methods
    private fun createBooksByYearStats(books: List<Book>): BarEntries {
        val booksByYear = books
            .mapNotNull { it.readingDate }
            .sortedBy { it.year }
            .mapNotNull { it.toString("yyyy") }
            .groupBy { it }

        val entries = mutableListOf<BarEntry>()
        for (entry in booksByYear.entries) {
            entries.add(
                BarEntry(
                    entry.key.toFloat(),
                    entry.value.size.toFloat(),
                ),
            )
        }
        return BarEntries(entries)
    }

    private fun createBooksByMonthStats(books: List<Book>): PieEntries {
        val booksByMonth = books
            .mapNotNull { it.readingDate }
            .sortedBy { it.month.number }
            .getGroupedBy("MMM", userRepository.language)

        val entries = mutableListOf<PieEntry>()
        for (entry in booksByMonth.entries) {
            entries.add(
                PieEntry(
                    entry.value.size.toFloat(),
                    entry.key,
                ),
            )
        }
        return PieEntries(entries)
    }

    private fun createBooksByAuthorStats(books: List<Book>): MapEntries = MapEntries(
        books
            .filter { it.authorsToString().isNotBlank() }
            .groupBy { it.authorsToString() }
            .toList()
            .sortedBy { it.second.size }
            .takeLast(5)
            .toMap(),
    )

    private fun createFormatStats(books: List<Book>): PieEntries {
        val booksByFormat = books
            .filter { !it.format.isNullOrEmpty() }
            .groupBy { it.format }

        val entries = mutableListOf<PieEntry>()
        for (entry in booksByFormat.entries) {
            entries.add(
                PieEntry(
                    entry.value.size.toFloat(),
                    Constants.FORMATS.first { it.id == entry.key }.name,
                ),
            )
        }
        return PieEntries(entries)
    }
    //endregion
}