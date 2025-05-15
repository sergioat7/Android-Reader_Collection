/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.presentation.statistics

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import aragones.sergio.readercollection.utils.Constants
import com.aragones.sergio.util.extensions.getGroupedBy
import com.aragones.sergio.util.extensions.getOrderedBy
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var _state: MutableState<StatisticsUiState> = mutableStateOf(StatisticsUiState.Empty)
    private val _booksError = MutableStateFlow<ErrorResponse?>(null)
    private val _confirmationDialogMessageId = MutableStateFlow(-1)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    val state: State<StatisticsUiState> = _state
    val booksError: StateFlow<ErrorResponse?> = _booksError
    var sortParam = userRepository.sortParam
    var isSortDescending = userRepository.isSortDescending
    var tutorialShown = userRepository.hasStatisticsTutorialBeenShown
    val confirmationDialogMessageId: StateFlow<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBooks() {
        _state.value = when (val currentState = _state.value) {
            is StatisticsUiState.Empty -> StatisticsUiState.Success.empty().copy(isLoading = true)
            is StatisticsUiState.Success -> currentState.copy(isLoading = true)
        }

        booksRepository
            .getReadBooks()
            .subscribeBy(
                onComplete = {
                    _state.value = StatisticsUiState.Empty
                    _booksError.value = ErrorResponse("", R.string.error_database)
                },
                onNext = { books ->

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
                },
                onError = {
                    _state.value = StatisticsUiState.Empty
                    _booksError.value = ErrorResponse("", R.string.error_database)
                },
            ).addTo(disposables)
    }

    fun setTutorialAsShown() {
        userRepository.setHasStatisticsTutorialBeenShown(true)
        tutorialShown = true
    }

    fun showConfirmationDialog(textId: Int) {
        _confirmationDialogMessageId.value = textId
    }

    fun closeDialogs() {
        _booksError.value = null
        _confirmationDialogMessageId.value = -1
        _infoDialogMessageId.value = -1
        _booksError.value = null
    }

    fun importData(jsonData: String) {
        booksRepository
            .importDataFrom(jsonData)
            .subscribeBy(
                onComplete = {
                    _infoDialogMessageId.value = R.string.data_imported
                },
                onError = {
                    _booksError.value = ErrorResponse("", R.string.error_file_data)
                },
            ).addTo(disposables)
    }

    fun getDataToExport(completion: (String?) -> Unit) {
        booksRepository
            .exportDataTo()
            .subscribeBy(
                onSuccess = {
                    completion(it)
                    _infoDialogMessageId.value = R.string.file_created
                },
                onError = {
                    completion(null)
                    _booksError.value = ErrorResponse("", R.string.error_database)
                },
            ).addTo(disposables)
    }
    //endregion

    //region Private methods
    private fun createBooksByYearStats(books: List<Book>): List<BarEntry> {
        val booksByYear = books
            .mapNotNull { it.readingDate }
            .getOrderedBy(Calendar.YEAR)
            .getGroupedBy("yyyy", userRepository.language)

        val entries = mutableListOf<BarEntry>()
        for (entry in booksByYear.entries) {
            entries.add(
                BarEntry(
                    entry.key.toFloat(),
                    entry.value.size.toFloat(),
                ),
            )
        }
        return entries
    }

    private fun createBooksByMonthStats(books: List<Book>): List<PieEntry> {
        val booksByMonth = books
            .mapNotNull { it.readingDate }
            .getOrderedBy(Calendar.MONTH)
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
        return entries
    }

    private fun createBooksByAuthorStats(books: List<Book>): Map<String, List<Book>> = books
        .filter { it.authorsToString().isNotBlank() }
        .groupBy { it.authorsToString() }
        .toList()
        .sortedBy { it.second.size }
        .takeLast(5)
        .toMap()

    private fun createFormatStats(books: List<Book>): List<PieEntry> {
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
        return entries
    }
    //endregion
}