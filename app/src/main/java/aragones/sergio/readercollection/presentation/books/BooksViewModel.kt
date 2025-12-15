/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.books

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.toDate
import com.aragones.sergio.util.extensions.toString
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private var originalBooks = emptyList<Book>()
    private val _state: MutableStateFlow<BooksUiState> = MutableStateFlow(
        BooksUiState.Empty(query = "", isLoading = false),
    )
    private var _sortingPickerState: MutableStateFlow<UiSortingPickerState> = MutableStateFlow(
        UiSortingPickerState(
            show = false,
            sortParam = userRepository.sortParam,
            isSortDescending = userRepository.isSortDescending,
        ),
    )
    private val _booksError = MutableStateFlow<ErrorResponse?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<BooksUiState> = _state
    val sortingPickerState: StateFlow<UiSortingPickerState> = _sortingPickerState
    val booksError: StateFlow<ErrorResponse?> = _booksError
    //endregion

    //region Public methods
    fun fetchBooks() {
        _state.update {
            when (it) {
                is BooksUiState.Empty -> it.copy(isLoading = true)
                is BooksUiState.Success -> it.copy(isLoading = true)
            }
        }

        combine(
            booksRepository.getBooks(),
            _sortingPickerState,
        ) { books, _ ->
            originalBooks = books
            sortBooks()
        }.launchIn(viewModelScope)
    }

    fun closeDialogs() {
        _booksError.value = null
    }

    fun showSortingPickerState() {
        _sortingPickerState.update { it.copy(show = true) }
    }

    fun updatePickerState(newSortParam: String?, newIsSortDescending: Boolean) {
        _sortingPickerState.value = UiSortingPickerState(
            show = false,
            sortParam = newSortParam,
            isSortDescending = newIsSortDescending,
        )
    }

    fun searchBooks(query: String) {
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(query = query)
            is BooksUiState.Success -> currentState.copy(query = query)
        }
        sortBooks()
    }

    fun switchBooksPriority(fromIndex: Int, toIndex: Int) {
        val books = when (val currentState = state.value) {
            is BooksUiState.Empty -> emptyList()
            is BooksUiState.Success -> currentState.books.books
        }.filter { it.isPending() }
            .sortedBy { it.priority }
            .map { it.copy() }
        if (fromIndex >= books.size || toIndex >= books.size) return
        for ((index, book) in books.withIndex()) {
            book.priority = when (index) {
                fromIndex -> toIndex
                toIndex -> fromIndex
                else -> index
            }
        }
        setPriorityFor(books)
    }

    fun setBook(book: Book) = viewModelScope.launch {
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(isLoading = true)
            is BooksUiState.Success -> currentState.copy(isLoading = true)
        }
        var selectedBook = book
        if (book.readingDate == null && book.state == BookState.READ) {
            selectedBook = selectedBook.copy(readingDate = Date().toString(format = null).toDate())
        }
        if (book.priority == -1 && book.state == BookState.PENDING) {
            val maxPriority = when (val currentState = _state.value) {
                is BooksUiState.Empty -> emptyList()
                is BooksUiState.Success -> currentState.books.books
            }.filter { it.isPending() }.maxByOrNull { it.priority }?.priority ?: -1
            selectedBook = selectedBook.copy(priority = maxPriority + 1)
        }
        booksRepository.setBook(selectedBook).fold(
            onSuccess = {
                /* no-op due to database is being observed */
            },
            onFailure = {
                _state.value = when (val currentState = _state.value) {
                    is BooksUiState.Empty -> currentState.copy(isLoading = false)
                    is BooksUiState.Success -> currentState.copy(isLoading = false)
                }
                _booksError.value = ApiManager.handleError(it)
            },
        )
    }
    //endregion

    //region Private methods
    private fun sortBooks() {
        val sortedBooks = getSortedBooks()
        _state.value = when {
            sortedBooks.isEmpty() -> BooksUiState.Empty(
                query = _state.value.query,
                isLoading = false,
            )
            else -> BooksUiState.Success(
                books = Books(sortedBooks),
                query = _state.value.query,
                isLoading = false,
            )
        }
    }

    private fun getSortedBooks(): List<Book> {
        val filteredBooks = originalBooks.filter { book ->
            (book.title?.contains(_state.value.query, true) ?: false) ||
                book.authorsToString().contains(_state.value.query, true)
        }
        val sortedBooks = when (_sortingPickerState.value.sortParam) {
            "title" -> filteredBooks.sortedBy { it.title }
            "publishedDate" -> filteredBooks.sortedBy { it.publishedDate }
            "readingDate" -> filteredBooks.sortedBy { it.readingDate }
            "pageCount" -> filteredBooks.sortedBy { it.pageCount }
            "rating" -> filteredBooks.sortedBy { it.rating }
            "authors" -> filteredBooks.sortedBy { it.authorsToString() }
            else -> filteredBooks.sortedBy { it.id }
        }
        return if (_sortingPickerState.value.isSortDescending) {
            sortedBooks.reversed()
        } else {
            sortedBooks
        }
    }

    private fun setPriorityFor(books: List<Book>) = viewModelScope.launch {
        _state.value = when (val currentState = _state.value) {
            is BooksUiState.Empty -> currentState.copy(isLoading = true)
            is BooksUiState.Success -> currentState.copy(isLoading = true)
        }
        booksRepository.setBooks(books).fold(
            onSuccess = {
                /* no-op due to database is being observed */
            },
            onFailure = {
                _state.value = when (val currentState = _state.value) {
                    is BooksUiState.Empty -> currentState.copy(isLoading = false)
                    is BooksUiState.Success -> currentState.copy(isLoading = false)
                }
                _booksError.value = ErrorResponse(
                    Constants.EMPTY_VALUE,
                    R.string.error_database,
                )
            },
        )
    }
    //endregion
}