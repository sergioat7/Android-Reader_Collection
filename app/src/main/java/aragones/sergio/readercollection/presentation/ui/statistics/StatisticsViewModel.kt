/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.statistics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.extensions.combineWith
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
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

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val _books = MutableLiveData<List<Book>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse?>()
    private val _booksByYearStats = MutableLiveData<List<BarEntry>>()
    private val _booksByMonthStats = MutableLiveData<List<PieEntry>>()
    private val _booksByAuthorStats = MutableLiveData<Map<String, List<Book>>>()
    private val _longerBook = MutableLiveData<Book?>()
    private val _shorterBook = MutableLiveData<Book?>()
    private val _booksByFormatStats = MutableLiveData<List<PieEntry>>()
    private val _confirmationDialogMessageId = MutableLiveData(-1)
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    val books: LiveData<List<Book>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse?> = _booksError
    val booksByYearStats: LiveData<List<BarEntry>> = _booksByYearStats
    val booksByMonthStats: LiveData<List<PieEntry>> = _booksByMonthStats
    val booksByAuthorStats: LiveData<Map<String, List<Book>>> = _booksByAuthorStats
    val longerBook: LiveData<Book?> = _longerBook
    val shorterBook: LiveData<Book?> = _shorterBook
    val booksByFormatStats: LiveData<List<PieEntry>> = _booksByFormatStats
    val noResultsVisible: LiveData<Boolean> = booksByYearStats.combineWith(
        booksByMonthStats,
        booksByAuthorStats,
        longerBook,
        shorterBook,
        booksByFormatStats,
    ) { booksByYearStats,
        booksByMonthStats,
        booksByAuthorStats,
        longerBook,
        shorterBook,
        booksByFormatStats,
        ->
        booksByYearStats?.isEmpty() == true &&
            booksByMonthStats?.isEmpty() == true &&
            booksByAuthorStats?.isEmpty() == true &&
            longerBook == null &&
            shorterBook == null &&
            booksByFormatStats?.isEmpty() == true
    }
    var sortParam = userRepository.sortParam
    var isSortDescending = userRepository.isSortDescending
    var tutorialShown = userRepository.hasStatisticsTutorialBeenShown
    val confirmationDialogMessageId: LiveData<Int> = _confirmationDialogMessageId
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBooks() {
        _booksLoading.value = true
        booksRepository
            .getReadBooks()
            .subscribeBy(
                onComplete = {
                    _books.value = emptyList()
                    manageError(ErrorResponse("", R.string.error_database))
                },
                onNext = { books ->
                    _booksByYearStats.value = createBooksByYearStats(books)
                    _booksByMonthStats.value = createBooksByMonthStats(books)
                    _booksByAuthorStats.value = createBooksByAuthorStats(books)
                    _longerBook.value = books
                        .filter { it.pageCount > 0 }
                        .maxByOrNull { it.pageCount }
                    _shorterBook.value = books
                        .filter { it.pageCount > 0 }
                        .minByOrNull { it.pageCount }
                    _booksByFormatStats.value = createFormatStats(books)
                    _books.value = books
                    _booksLoading.value = false
                },
                onError = {
                    _books.value = emptyList()
                    manageError(ErrorResponse("", R.string.error_database))
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
                    manageError(ErrorResponse("", R.string.error_file_data))
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
                    manageError(ErrorResponse("", R.string.error_database))
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

    private fun manageError(error: ErrorResponse) {
        _booksLoading.value = false
        _booksError.value = error
    }
    //endregion
}