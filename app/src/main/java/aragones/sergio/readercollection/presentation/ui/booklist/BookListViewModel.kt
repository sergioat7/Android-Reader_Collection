/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import aragones.sergio.readercollection.presentation.ui.components.UiSortingPickerState
import aragones.sergio.readercollection.presentation.ui.navigation.Route
import com.aragones.sergio.util.BookState
import com.aragones.sergio.util.Constants
import com.aragones.sergio.util.extensions.getMonthNumber
import com.aragones.sergio.util.extensions.getYear
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class BookListViewModel @Inject constructor(
    state: SavedStateHandle,
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private val params = state.toRoute<Route.BookList>()
    private val arePendingBooks: Boolean
        get() = params.state == BookState.PENDING
    private var _state: MutableState<BookListUiState> =
        mutableStateOf(BookListUiState.Success.initial())
    private var _sortingPickerState: MutableState<UiSortingPickerState> = mutableStateOf(
        UiSortingPickerState(
            show = false,
            sortParam = params.sortParam,
            isSortDescending = params.isSortDescending,
        ),
    )
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    val state: State<BookListUiState> = _state
    val sortingPickerState: State<UiSortingPickerState> = _sortingPickerState
    val booksError: LiveData<ErrorResponse> = _booksError
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
    var tutorialShown = userRepository.hasDragTutorialBeenShown
    //endregion

    //region Lifecycle methods
    override fun onCleared() {
        super.onCleared()

        booksRepository.onDestroy()
        userRepository.onDestroy()
    }
    //endregion

    //region Public methods
    fun fetchBooks() {
        _state.value = when (val currentState = _state.value) {
            is BookListUiState.Empty -> BookListUiState.Success(
                isLoading = true,
                books = emptyList(),
                isDraggingEnabled = false,
            )
            is BookListUiState.Success -> currentState.copy(isLoading = true)
        }

        booksRepository
            .getBooks()
            .subscribeBy(
                onComplete = {
                    showError()
                },
                onNext = { books ->

                    if (books.isEmpty()) {
                        showError()
                    } else {
                        _state.value = when (val currentState = _state.value) {
                            is BookListUiState.Empty -> BookListUiState.Success(
                                isLoading = true,
                                books = getFilteredBooksFor(books),
                                isDraggingEnabled = false,
                            )
                            is BookListUiState.Success -> currentState.copy(
                                isLoading = false,
                                books = getFilteredBooksFor(books),
                            )
                        }
                    }
                },
                onError = {
                    showError()
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
    }

    fun switchDraggingState() {
        (_state.value as? BookListUiState.Success)?.let { state ->
            _state.value = state.copy(isDraggingEnabled = state.isDraggingEnabled.not())
        }
    }

    fun updateBookOrdering(books: List<Book>) {
        for ((index, book) in books.withIndex()) {
            book.priority = index
        }
        _state.value = when (val currentState = _state.value) {
            is BookListUiState.Empty -> BookListUiState.Success(
                isLoading = false,
                books = getFilteredBooksFor(books),
                isDraggingEnabled = true,
            )
            is BookListUiState.Success -> currentState.copy(books = getFilteredBooksFor(books))
        }
    }

    fun setPriorityFor(books: List<Book>) {
        booksRepository
            .setBooks(books)
            .subscribeBy(
                onComplete = {
                    /* no-op due to database is being observed */
                },
                onError = {
                    showError()
                },
            ).addTo(disposables)
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

    fun setTutorialAsShown() {
        userRepository.setHasDragTutorialBeenShown(true)
        tutorialShown = true
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
        if (params.month in 0..11) {
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
        error: ErrorResponse = ErrorResponse(Constants.EMPTY_VALUE, R.string.error_database),
    ) {
        _state.value = BookListUiState.Success(
            isLoading = false,
            books = emptyList(),
            isDraggingEnabled = false,
        )
        _booksError.value = error
    }
    //endregion
}