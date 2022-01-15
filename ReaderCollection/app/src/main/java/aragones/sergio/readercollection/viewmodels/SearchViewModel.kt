/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.GoogleBookRepository
import aragones.sergio.readercollection.utils.ScrollPosition
import aragones.sergio.readercollection.utils.State
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class SearchViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val googleBookRepository: GoogleBookRepository
) : BaseViewModel() {

    //region Private properties
    private var page: Int = 1
    private val _query = MutableLiveData("")
    private val _books = MutableLiveData<MutableList<BookResponse>>(mutableListOf())
    private val _searchLoading = MutableLiveData(false)
    private val _bookAdded = MutableLiveData<Int?>()
    private val _searchError = MutableLiveData<ErrorResponse?>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    //endregion

    //region Public properties
    var query: LiveData<String> = _query
    val books: LiveData<MutableList<BookResponse>> = _books
    val searchLoading: LiveData<Boolean> = _searchLoading
    val bookAdded: LiveData<Int?> = _bookAdded
    val searchError: LiveData<ErrorResponse?> = _searchError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    //endregion

    //region Public methods
    fun searchBooks() {

        _searchLoading.value = true
        googleBookRepository.searchBooksObserver(_query.value ?: "", page, null).subscribeBy(
            onSuccess = { googleBookListResponse ->

                page++
                val currentValues = _books.value ?: mutableListOf()
                val newValues =
                    googleBookListResponse.items?.map { BookResponse(it) } ?: mutableListOf()

                if (currentValues.isEmpty()) {
                    currentValues.add(BookResponse(id = ""))
                }

                currentValues.addAll(currentValues.size - 1, newValues)
                if (newValues.isEmpty()) {
                    currentValues.removeLast()
                }

                _books.value = currentValues
                _searchLoading.value = false
            },
            onError = {

                _searchLoading.value = false
                _searchError.value = ErrorResponse("", R.string.error_search)
                _searchError.value = null
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun reloadData() {

        page = 1
        _books.value = mutableListOf()
    }

    fun setSearch(query: String) {
        _query.value = query
    }

    fun addBook(position: Int) {
        _books.value?.get(position)?.let { book ->

            book.state = State.PENDING
            _searchLoading.value = true
            booksRepository.createBookObserver(book).subscribeBy(
                onComplete = {

                    _bookAdded.value = position
                    _bookAdded.value = null
                    _searchLoading.value = false
                },
                onError = {

                    _searchLoading.value = false
                    _bookAdded.value = null
                    _searchError.value = ApiManager.handleError(it)
                    _searchError.value = null
                    onDestroy()
                }
            ).addTo(disposables)
        }
    }

    fun setPosition(newPosition: ScrollPosition) {
        _scrollPosition.value = newPosition
    }
    //endregion
}