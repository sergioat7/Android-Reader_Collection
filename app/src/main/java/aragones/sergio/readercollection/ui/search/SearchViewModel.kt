/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.GoogleBookRepository
import aragones.sergio.readercollection.data.source.UserRepository
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.utils.ScrollPosition
import aragones.sergio.readercollection.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val googleBookRepository: GoogleBookRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private var page: Int = 1
    private val _query = MutableLiveData("")
    private val _books = MutableLiveData<MutableList<BookResponse>>(mutableListOf())
    private val _searchLoading = MutableLiveData(false)
    private val _bookAdded = MutableLiveData<Int?>()
    private val _searchError = MutableLiveData<ErrorResponse?>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    private lateinit var pendingBooks: MutableList<BookResponse>
    //endregion

    //region Public properties
    var query: LiveData<String> = _query
    val books: LiveData<MutableList<BookResponse>> = _books
    val searchLoading: LiveData<Boolean> = _searchLoading
    val bookAdded: LiveData<Int?> = _bookAdded
    val searchError: LiveData<ErrorResponse?> = _searchError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    var tutorialShown = userRepository.hasSearchTutorialBeenShown
    //endregion

    //region Lifecycle methods
    init {
        fetchPendingBooks()
    }

    fun onResume() {
        fetchPendingBooks()
    }

    override fun onDestroy() {
        super.onDestroy()

        booksRepository.onDestroy()
        googleBookRepository.onDestroy()
        userRepository.onDestroy()
    }
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
        _books.value?.get(position)?.let { newBook ->

            newBook.state = State.PENDING
            newBook.priority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1) + 1
            _searchLoading.value = true
            booksRepository.createBook(newBook, success = {

                pendingBooks.add(newBook)
                _bookAdded.value = position
                _bookAdded.value = null
                _searchLoading.value = false
            }, failure = {

                _searchLoading.value = false
                _bookAdded.value = null
                _searchError.value = it
                _searchError.value = null
            })
        }
    }

    fun setPosition(newPosition: ScrollPosition) {
        _scrollPosition.value = newPosition
    }

    fun setTutorialAsShown() {
        userRepository.setHasSearchTutorialBeenShown(true)
        tutorialShown = true
    }
    //endregion

    //region Private methods
    private fun fetchPendingBooks() {

        booksRepository.getPendingBooksDatabaseObserver().subscribeBy(
            onComplete = {
                pendingBooks = mutableListOf()
            },
            onSuccess = {
                pendingBooks = it.toMutableList()
            },
            onError = {
                pendingBooks = mutableListOf()
            }
        ).addTo(disposables)
    }
    //endregion
}