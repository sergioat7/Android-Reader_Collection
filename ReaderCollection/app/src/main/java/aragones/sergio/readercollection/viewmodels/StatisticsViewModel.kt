/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 25/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import com.github.mikephil.charting.data.PieEntry
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Private properties
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _formatStats = MutableLiveData<List<PieEntry>>()
    //endregion

    //region Public properties
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val formatStats: LiveData<List<PieEntry>> = _formatStats
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
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
                noBooksError()
            },
            onSuccess = {

                if (it.isEmpty()) {
                    noBooksError()
                } else {
                    //TODO: handle results
                    createFormatStats(it)
                    _booksLoading.value = false
                }
            },
            onError = {
                noBooksError()
            }
        ).addTo(disposables)
    }
    //endregion

    //region Private methods
    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
        onDestroy()
    }

    private fun createFormatStats(books: List<BookResponse>) {

        val entries = mutableListOf<PieEntry>()
        for (entry in books.groupBy { it.format }.entries) {
            entries.add(
                PieEntry(
                    entry.value.size.toFloat(),
                    Constants.FORMATS.first { it.id == entry.key }.name
                )
            )
        }
        _formatStats.value = entries
    }
    //endregion
}