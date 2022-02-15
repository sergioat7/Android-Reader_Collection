/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.extensions.getMonthNumber
import aragones.sergio.readercollection.extensions.getYear
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.utils.ScrollPosition
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookListViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Private properties
    private var state: String = ""
    private var sortParam: String? = null
    private var isSortDescending: Boolean = false
    private var query: String = ""
    private var year: Int = -1
    private var month: Int = -1
    private var author: String? = null
    private var format: String? = null
    private val _books = MutableLiveData<List<BookResponse>>(listOf())
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    //endregion

    //region Public properties
    val books: LiveData<List<BookResponse>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun setParams(
        state: String,
        sortParam: String?,
        isSortDescending: Boolean,
        query: String,
        year: Int,
        month: Int,
        author: String?,
        format: String?
    ) {
        this.state = state
        this.sortParam = sortParam
        this.isSortDescending = isSortDescending
        this.query = query
        this.year = year
        this.month = month
        this.author = author
        this.format = format
    }

    fun fetchBooks() {

        _booksLoading.value = true
        booksRepository.getBooksDatabaseObserver(
            format,
            state,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {

                noBooksError()
            },
            onSuccess = {

                if (it.isEmpty()) {
                    noBooksError()
                } else {

                    val sortedBooks = if (isSortDescending) it.reversed() else it
                    var filteredBooks = sortedBooks
                        .filter { book ->
                            book.title?.contains(query, true) ?: false
                        }.filter { book ->
                            book.authorsToString().contains(author ?: "")
                        }
                    if (year >= 0) {
                        filteredBooks = filteredBooks.filter { book ->
                            book.readingDate.getYear() == year
                        }
                    }
                    if (month in 0..11) {
                        filteredBooks = filteredBooks.filter { book ->
                            book.readingDate.getMonthNumber() == month
                        }
                    }
                    _books.value = filteredBooks
                    _booksLoading.value = false
                }
            },
            onError = {

                noBooksError()
            }
        ).addTo(disposables)
    }

    fun setPosition(newPosition: ScrollPosition) {
        _scrollPosition.value = newPosition
    }

    fun sort(context: Context, acceptHandler: (() -> Unit)?) {
        super.sort(context, sortParam, isSortDescending) { newSortParam, newIsSortDescending ->

            sortParam = newSortParam
            isSortDescending = newIsSortDescending
            fetchBooks()
            acceptHandler?.invoke()
        }
    }
    //endregion

    //region Private methods
    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
        onDestroy()
    }
    //endregion
}