/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BookListViewModel @Inject constructor(
    private val booksRepository: BooksRepository
) : BaseViewModel() {

    //region Private properties
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    //endregion

    //region Public properties
    val books: LiveData<List<BookResponse>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBooks(state: String, sortParam: String?, isSortDescending: Boolean, query: String) {

        _booksLoading.value = true
        booksRepository.getBooksDatabaseObserver(
            null,
            state,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {

                _books.value = listOf()
                _booksLoading.value = false
            },
            onSuccess = {

                val sortedBooks = if (isSortDescending) it.reversed() else it
                _books.value = sortedBooks.filter { book ->
                    book.title?.contains(query, true) ?: false
                }
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = ApiManager.handleError(it)
                onDestroy()
            }
        ).addTo(disposables)
    }
    //endregion
}