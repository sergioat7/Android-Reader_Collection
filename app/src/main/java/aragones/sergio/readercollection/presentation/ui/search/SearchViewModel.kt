/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.ScrollPosition
import com.aragones.sergio.util.State
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private var page: Int = 1
    private val _query = MutableLiveData("")
    private val _books = MutableLiveData<MutableList<Book>>(mutableListOf())
    private val _searchLoading = MutableLiveData(false)
    private val _searchError = MutableLiveData<ErrorResponse?>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    private lateinit var pendingBooks: MutableList<Book>
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    var query: LiveData<String> = _query
    val books: LiveData<MutableList<Book>> = _books
    val searchLoading: LiveData<Boolean> = _searchLoading
    val searchError: LiveData<ErrorResponse?> = _searchError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    var tutorialShown = userRepository.hasSearchTutorialBeenShown
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
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
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun searchBooks() {

        _searchLoading.value = true
        booksRepository.searchBooks(_query.value ?: "", page, null).subscribeBy(
            onSuccess = { newBooks ->

                page++
                val currentValues = _books.value ?: mutableListOf()
                if (currentValues.isEmpty()) {
                    currentValues.add(Book(id = ""))
                }
                currentValues.addAll(currentValues.size - 1, newBooks)
                if (newBooks.isEmpty()) {
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
                _infoDialogMessageId.value = R.string.book_saved
                _searchLoading.value = false
            }, failure = {

                _searchLoading.value = false
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

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun fetchPendingBooks() {

        booksRepository.getPendingBooks().subscribeBy(
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