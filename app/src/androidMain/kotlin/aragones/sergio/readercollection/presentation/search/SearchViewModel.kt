/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.domain.model.Books
import aragones.sergio.readercollection.domain.model.ErrorModel
import com.aragones.sergio.util.BookState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val booksRepository: BooksRepository,
) : ViewModel() {

    //region Private properties
    private var query = ""
    private var page: Int = 1
    private val books = mutableListOf<Book>()
    private var savedBooks = mutableListOf<Book>()
    private val pendingBooks: List<Book>
        get() = savedBooks.filter { it.isPending() }
    private val _state: MutableStateFlow<SearchUiState> = MutableStateFlow(SearchUiState.Empty)
    private val _infoDialogMessageId = MutableStateFlow(-1)
    //endregion

    //region Public properties
    var state: StateFlow<SearchUiState> = _state
    val infoDialogMessageId: StateFlow<Int> = _infoDialogMessageId
    //endregion

    //region Lifecycle methods
    fun onResume() {
        fetchSavedBooks()
    }
    //endregion

    //region Public methods
    fun searchBooks(reload: Boolean = false, query: String? = null) {
        if (reload) {
            page = 1
            books.clear()
        }

        if (query != null) {
            this.query = query
        }

        _state.update {
            when (it) {
                SearchUiState.Empty -> SearchUiState.Success(
                    isLoading = true,
                    query = this.query,
                    books = Books(),
                )
                is SearchUiState.Success -> it.copy(isLoading = true)
                is SearchUiState.Error -> it.copy(isLoading = true)
            }
        }

        viewModelScope.launch {
            booksRepository.searchBooks(this@SearchViewModel.query, page, null).fold(
                onSuccess = { newBooks ->
                    if (books.isEmpty()) {
                        books.add(Book(id = ""))
                    }
                    books.addAll(books.size - 1, newBooks)
                    if (newBooks.isEmpty()) {
                        books.removeAt(books.lastIndex)
                    }
                    val updatedBooks = mutableListOf<Book>().apply { addAll(books) }

                    page++
                    _state.value = SearchUiState.Success(
                        isLoading = false,
                        query = this@SearchViewModel.query,
                        books = Books(updatedBooks),
                    )
                },
                onFailure = {
                    _state.value = SearchUiState.Error(
                        isLoading = false,
                        query = this@SearchViewModel.query,
                        value = ErrorModel("", R.string.error_search),
                    )
                },
            )
        }
    }

    fun addBook(bookId: String) {
        if (savedBooks.firstOrNull { it.id == bookId } != null) {
            _infoDialogMessageId.value = R.string.error_resource_found
            return
        }

        val newBook = books.firstOrNull { it.id == bookId } ?: return
        newBook.state = BookState.PENDING
        newBook.priority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1) + 1

        viewModelScope.launch {
            booksRepository.createBook(newBook).fold(
                onSuccess = {
                    _infoDialogMessageId.value = R.string.book_saved
                },
                onFailure = {
                    _infoDialogMessageId.value = R.string.error_database
                },
            )
        }
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun fetchSavedBooks() = viewModelScope.launch {
        booksRepository.getBooks().collect {
            savedBooks = it.toMutableList()
        }
    }
    //endregion
}