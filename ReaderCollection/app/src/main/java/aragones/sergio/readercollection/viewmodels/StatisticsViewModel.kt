/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.extensions.combineWith
import aragones.sergio.readercollection.extensions.getGroupedBy
import aragones.sergio.readercollection.extensions.getOrderedBy
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieEntry
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import java.io.File
import java.util.*
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse?>()
    private val _exportSuccessMessage = MutableLiveData<Pair<Int, String>?>()
    private val _booksByYearStats = MutableLiveData<List<BarEntry>>()
    private val _booksByMonthStats = MutableLiveData<List<PieEntry>>()
    private val _booksByAuthorStats = MutableLiveData<Map<String, List<BookResponse>>>()
    private val _longerBook = MutableLiveData<BookResponse?>()
    private val _shorterBook = MutableLiveData<BookResponse?>()
    private val _booksByFormatStats = MutableLiveData<List<PieEntry>>()
    //endregion

    //region Public properties
    val books: LiveData<List<BookResponse>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse?> = _booksError
    val exportSuccessMessage: LiveData<Pair<Int, String>?> = _exportSuccessMessage
    val booksByYearStats: LiveData<List<BarEntry>> = _booksByYearStats
    val booksByMonthStats: LiveData<List<PieEntry>> = _booksByMonthStats
    val booksByAuthorStats: LiveData<Map<String, List<BookResponse>>> = _booksByAuthorStats
    val longerBook: LiveData<BookResponse?> = _longerBook
    val shorterBook: LiveData<BookResponse?> = _shorterBook
    val booksByFormatStats: LiveData<List<PieEntry>> = _booksByFormatStats
    val noResultsVisible: LiveData<Boolean> = booksByYearStats.combineWith(
        booksByMonthStats,
        booksByAuthorStats,
        longerBook,
        shorterBook,
        booksByFormatStats
    ) { booksByYearStats, booksByMonthStats, booksByAuthorStats, longerBook, shorterBook, booksByFormatStats ->
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
        booksRepository.getBooksDatabaseObserver(
            null,
            State.READ,
            null,
            null
        ).subscribeBy(
            onComplete = {

                _books.value = emptyList()
                manageError(ErrorResponse("", R.string.error_database))
            },
            onSuccess = { books ->

                createBooksByYearStats(books)
                createBooksByMonthStats(books)
                createBooksByAuthorStats(books.filter { it.authorsToString().isNotBlank() })
                _longerBook.value = books.filter { it.pageCount > 0 }.maxByOrNull { it.pageCount }
                _shorterBook.value = books.filter { it.pageCount > 0 }.minByOrNull { it.pageCount }
                createFormatStats(books.filter { it.format != null })
                _books.value = books
                _booksLoading.value = false
            },
            onError = {

                _books.value = emptyList()
                manageError(ErrorResponse("", R.string.error_database))
            }
        ).addTo(disposables)
    }

    fun setTutorialAsShown() {
        userRepository.setHasStatisticsTutorialBeenShown(true)
        tutorialShown = true
    }

    fun importData() {

        _booksLoading.value = true
        val exportDir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, "books.csv")

        if (file.exists()) {
            booksRepository.importDataFrom(file).subscribeBy(
                onComplete = {

                    _booksLoading.value = false
                    _exportSuccessMessage.value = Pair(R.string.data_imported, file.path)
                    _exportSuccessMessage.value = null
                },
                onError = {
                    manageError(ErrorResponse("", R.string.error_file_data))
                }
            ).addTo(disposables)
        } else {
            //TODO: show error
        }
    }

    fun exportData() {

        _booksLoading.value = true
        val exportDir =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "")
        if (!exportDir.exists()) exportDir.mkdirs()
        val file = File(exportDir, "books.csv")

        if (booksRepository.exportDataTo(file)) {
            _booksLoading.value = false
            _exportSuccessMessage.value = Pair(R.string.file_created, file.path)
            _exportSuccessMessage.value = null
        } else {
            manageError(ErrorResponse("", R.string.error_database))
        }
    }
    //endregion

    //region Private methods
    private fun createBooksByYearStats(books: List<BookResponse>) {

        val booksByYear = books
            .mapNotNull { it.readingDate }
            .getOrderedBy(Calendar.YEAR)
            .getGroupedBy("yyyy")

        val entries = mutableListOf<BarEntry>()
        for (entry in booksByYear.entries) {
            entries.add(
                BarEntry(
                    entry.key.toFloat(),
                    entry.value.size.toFloat()
                )
            )
        }
        _booksByYearStats.value = entries
    }

    private fun createBooksByMonthStats(books: List<BookResponse>) {

        val booksByMonth = books
            .mapNotNull { it.readingDate }
            .getOrderedBy(Calendar.MONTH)
            .getGroupedBy("MMM")

        val entries = mutableListOf<PieEntry>()
        for (entry in booksByMonth.entries) {
            entries.add(
                PieEntry(
                    entry.value.size.toFloat(),
                    entry.key
                )
            )
        }
        _booksByMonthStats.value = entries
    }

    private fun createBooksByAuthorStats(books: List<BookResponse>) {

        _booksByAuthorStats.value = books
            .groupBy { it.authorsToString() }
            .toList()
            .sortedBy { it.second.size }
            .takeLast(5)
            .toMap()
    }

    private fun createFormatStats(books: List<BookResponse>) {

        val booksByFormat = books
            .filter { it.format?.isNotEmpty() == true }
            .groupBy { it.format }

        val entries = mutableListOf<PieEntry>()
        for (entry in booksByFormat.entries) {
            entries.add(
                PieEntry(
                    entry.value.size.toFloat(),
                    Constants.FORMATS.first { it.id == entry.key }.name
                )
            )
        }
        _booksByFormatStats.value = entries
    }

    private fun manageError(error: ErrorResponse) {

        _booksLoading.value = false
        _booksError.value = error
        _booksError.value = null
    }
    //endregion
}