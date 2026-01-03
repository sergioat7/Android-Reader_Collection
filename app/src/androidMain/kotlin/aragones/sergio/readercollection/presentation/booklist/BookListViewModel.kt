/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.presentation.booklist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.FORMATS
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import aragones.sergio.readercollection.presentation.components.UiSortingPickerState
import aragones.sergio.readercollection.presentation.navigation.Route
import aragones.sergio.readercollection.utils.UiDateMapper.toMonthName
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.getMonthNumber
import com.aragones.sergio.util.extensions.getYear
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookListViewModel(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.BookList>()
    private val arePendingBooks: Boolean
        get() = params.state == BookState.PENDING
    private val subtitle: String
        get() {
            var subtitle = ""
            if (params.year > 0) {
                subtitle += "${params.year},"
            }
            params.month.takeIf { it >= 0 }?.let { month ->
                subtitle += month.toMonthName(userRepository.language)
            }
            if (params.author != null) {
                subtitle += "${params.author},"
            }
            params.format?.let { format ->
                subtitle += "${FORMATS.firstOrNull { it.id == format }?.name ?: ""},"
            }
            return subtitle.dropLast(1)
        }
    private var _state: MutableStateFlow<BookListUiState> =
        MutableStateFlow(BookListUiState.initial())
    private var _sortingPickerState: MutableStateFlow<UiSortingPickerState> = MutableStateFlow(
        UiSortingPickerState(
            show = false,
            sortParam = params.sortParam,
            isSortDescending = params.isSortDescending,
        ),
    )
    private val _booksError = MutableStateFlow<ErrorModel?>(null)
    //endregion

    //region Public properties
    val state: StateFlow<BookListUiState> = _state
    val sortingPickerState: StateFlow<UiSortingPickerState> = _sortingPickerState
    val booksError: StateFlow<ErrorModel?> = _booksError
    //endregion

    //region Public methods
    fun fetchBooks() {
        _state.update {
            it.copy(
                isLoading = true,
                subtitle = subtitle,
            )
        }

        combine(
            booksRepository.getBooks(),
            _sortingPickerState,
        ) { books, _ ->
            if (books.isEmpty()) {
                showError()
            } else {
                _state.update {
                    it.copy(
                        isLoading = false,
                        books = Books(getFilteredBooksFor(books)),
                        subtitle = subtitle,
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun switchDraggingState() {
        _state.update { it.copy(isDraggingEnabled = it.isDraggingEnabled.not()) }
    }

    fun updateBookOrdering(books: List<Book>) {
        for ((index, book) in books.withIndex()) {
            book.priority = index
        }
        _state.update { it.copy(books = Books(getFilteredBooksFor(books))) }
    }

    fun setPriorityFor(books: List<Book>) = viewModelScope.launch {
        booksRepository.setBooks(books).fold(
            onSuccess = {
                /* no-op due to database is being observed */
            },
            onFailure = {
                showError()
            },
        )
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
    //endregion

    //region Private methods
    private fun getFilteredBooksFor(books: List<Book>): List<Book> {
        var filteredBooks = books
            .filter { book ->

                var condition = true
                params.format?.let {
                    condition = book.format == it
                }
                if (params.state.isNotEmpty()) {
                    condition = condition && book.state == params.state
                }
                condition
            }.filter { book ->
                (book.title?.contains(params.query, true) ?: false) ||
                    book.authorsToString().contains(params.query, true)
            }.filter { book ->
                book.authorsToString().contains(params.author ?: "")
            }
        if (params.year >= 0) {
            filteredBooks = filteredBooks.filter { book ->
                book.readingDate.getYear() == params.year
            }
        }
        if (params.month in 1..12) {
            filteredBooks = filteredBooks.filter { book ->
                book.readingDate.getMonthNumber() == params.month
            }
        }

        val sortComparator = compareBy<Book> {
            when (_sortingPickerState.value.sortParam) {
                "title" -> it.title
                "publishedDate" -> it.publishedDate
                "readingDate" -> it.readingDate
                "pageCount" -> it.pageCount
                "rating" -> it.rating
                "authors" -> it.authorsToString()
                else -> it.id
            }
        }
        val comparator = if (arePendingBooks) {
            compareBy<Book> { it.priority }.then(sortComparator)
        } else {
            sortComparator.thenBy { it.priority }
        }
        return filteredBooks
            .sortedWith(comparator)
            .let {
                if (_sortingPickerState.value.isSortDescending && !arePendingBooks) {
                    it.reversed()
                } else {
                    it
                }
            }
    }

    private fun showError(
        error: ErrorModel = ErrorModel(Constants.EMPTY_VALUE, R.string.error_database),
    ) {
        _state.value = BookListUiState(
            isLoading = false,
            books = Books(),
            subtitle = subtitle,
            isDraggingEnabled = false,
        )
        _booksError.value = error
    }
    //endregion
}