/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
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
    fun setParams(state: String, sortParam: String?, isSortDescending: Boolean, query: String) {
        this.state = state
        this.sortParam = sortParam
        this.isSortDescending = isSortDescending
        this.query = query
    }

    fun fetchBooks() {

        _booksLoading.value = true
        booksRepository.getBooksDatabaseObserver(
            null,
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
                    _books.value = sortedBooks.filter { book ->
                        book.title?.contains(query, true) ?: false
                    }
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
    //endregion

    //region Private methods
    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
        onDestroy()
    }
    //endregion
}