/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.components.UiSortingPickerState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val originalBooks = MutableLiveData<List<Book>>()
    private val _state: MutableState<BooksUiState> = mutableStateOf(
        BooksUiState.Empty(query = "", isLoading = false),
    )
    private var _sortingPickerState: MutableState<UiSortingPickerState> = mutableStateOf(
        UiSortingPickerState(
            show = false,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
        ),
    )
    private val _booksError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val state: State<BooksUiState> = _state
    val sortingPickerState: State<UiSortingPickerState> = _sortingPickerState
    val booksError: LiveData<ErrorResponse?> = _booksError
    var tutorialShown = userRepository.hasBooksTutorialBeenShown
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
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(isLoading = true)
            is BooksUiState.Success -> currentState.copy(isLoading = true)
        }

        booksRepository
            .getBooks()
            .subscribeBy(
                onComplete = {
                    originalBooks.value = listOf()
                    _state.value = BooksUiState.Empty(query = _state.value.query, isLoading = false)
                },
                onNext = {
                    originalBooks.value = it
                    sortBooks()
                },
                onError = {
                    _state.value = when (val currentState = _state.value) {
                        is BooksUiState.Empty -> currentState.copy(isLoading = false)
                        is BooksUiState.Success -> currentState.copy(isLoading = false)
                    }
                    _booksError.value = ApiManager.handleError(it)
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _booksError.value = null
    }

    fun showSortingPickerState() {
        _sortingPickerState.value = _sortingPickerState.value.copy(show = true)
    }

    fun updatePickerState(newSortParam: String?, newIsSortDescending: Boolean) {
        _sortingPickerState.value = UiSortingPickerState(
            show = false,
            sortParam = newSortParam,
            isSortDescending = newIsSortDescending,
        )
        fetchBooks()
    }

    fun searchBooks(query: String) {
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(query = query)
            is BooksUiState.Success -> currentState.copy(query = query)
        }
        sortBooks()
    }

    fun switchBooksPriority(fromIndex: Int, toIndex: Int) {
        val books = when(val currentState = state.value) {
            is BooksUiState.Empty -> emptyList()
            is BooksUiState.Success -> currentState.books
        }.filter { it.isPending() }.sortedBy { it.priority }
        for ((index, book) in books.withIndex()) {
            book.priority = when (index) {
                fromIndex -> toIndex
                toIndex -> fromIndex
                else -> index
            }
        }
        setPriorityFor(books)
    }

    fun setTutorialAsShown() {
        userRepository.setHasBooksTutorialBeenShown(true)
        tutorialShown = true
    }

    fun setPriorityFor(books: List<Book>) {
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(isLoading = true)
            is BooksUiState.Success -> currentState.copy(isLoading = true)
        }

        booksRepository.setBooks(books, success = {
            searchBooks(_state.value.query)
        }, failure = {
            _state.value = when (val currentState = _state.value) {
                is BooksUiState.Empty -> currentState.copy(isLoading = false)
                is BooksUiState.Success -> currentState.copy(isLoading = false)
            }
            _booksError.value = it
        })
    }
    //endregion

    private fun sortBooks() {
        val sortedBooks = getSortedBooks()
        _state.value = when {
            sortedBooks.isEmpty() -> BooksUiState.Empty(
                query = _state.value.query,
                isLoading = false,
            )
            else -> BooksUiState.Success(
                books = sortedBooks,
                query = _state.value.query,
                isLoading = false,
            )
        }
    }

    private fun getSortedBooks(): List<Book> {
        val filteredBooks = originalBooks.value?.filter { book ->
            (book.title?.contains(_state.value.query, true) ?: false) ||
                book.authorsToString().contains(_state.value.query, true)
        } ?: listOf()
        val sortedBooks = when (_sortingPickerState.value.sortParam) {
            "title" -> filteredBooks.sortedBy { it.title }
            "publishedDate" -> filteredBooks.sortedBy { it.publishedDate }
            "readingDate" -> filteredBooks.sortedBy { it.readingDate }
            "pageCount" -> filteredBooks.sortedBy { it.pageCount }
            "rating" -> filteredBooks.sortedBy { it.rating }
            else -> filteredBooks.sortedBy { it.id }
        }
        return if (_sortingPickerState.value.isSortDescending) {
            sortedBooks.reversed()
        } else {
            sortedBooks
        }
    }
}