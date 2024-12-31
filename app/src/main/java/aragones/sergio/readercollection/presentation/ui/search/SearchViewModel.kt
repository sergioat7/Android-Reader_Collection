/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.search

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.BookState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository,
) : BaseViewModel() {

    //region Private properties
    private var query = ""
    private var page: Int = 1
    private val books = mutableListOf<Book>()
    private lateinit var savedBooks: MutableList<Book>
    private val pendingBooks: List<Book>
        get() = savedBooks.filter { it.isPending() }
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    var state: MutableState<SearchUiState> = mutableStateOf(SearchUiState.Empty)
        private set
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
    fun searchBooks(reload: Boolean = false, query: String? = null) {
        if (reload) {
            page = 1
            books.clear()
        }

        if (query != null) {
            this.query = query
        }

        state.value = when (val currentState = state.value) {
            is SearchUiState.Empty -> SearchUiState.Success(
                isLoading = true,
                query = this.query,
                books = listOf(),
            )
            is SearchUiState.Success -> currentState.copy(isLoading = true)
            is SearchUiState.Error -> currentState.copy(isLoading = true)
        }

        booksRepository
            .searchBooks(this.query, page, null)
            .subscribeBy(
                onSuccess = { newBooks ->

                    page++
                    if (books.isEmpty()) {
                        books.add(Book(id = ""))
                    }
                    books.addAll(books.size - 1, newBooks)
                    if (newBooks.isEmpty()) {
                        books.removeAt(books.lastIndex)
                    }

                    state.value = SearchUiState.Success(
                        isLoading = false,
                        query = this.query,
                        books = books,
                    )
                },
                onError = {
                    state.value = SearchUiState.Error(
                        isLoading = false,
                        query = this.query,
                        value = ErrorResponse("", R.string.error_search),
                    )
                },
            ).addTo(disposables)
    }

    fun addBook(bookId: String) {
        if (savedBooks.firstOrNull { it.id == bookId } != null) {
            _infoDialogMessageId.value = R.string.error_resource_found
            return
        }

        val newBook = books.firstOrNull { it.id == bookId } ?: return
        newBook.state = BookState.PENDING
        newBook.priority = (pendingBooks.maxByOrNull { it.priority }?.priority ?: -1) + 1
        booksRepository
            .createBook(newBook)
            .subscribeBy(
                onComplete = {
                    savedBooks.add(newBook)
                    _infoDialogMessageId.value = R.string.book_saved
                },
                onError = {
                    _infoDialogMessageId.value = R.string.error_database
                },
            ).addTo(disposables)
    }

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
    }
    //endregion

    //region Private methods
    private fun fetchPendingBooks() {
        booksRepository
            .getBooks()
            .subscribeBy(
                onComplete = {
                    savedBooks = mutableListOf()
                },
                onNext = {
                    savedBooks = it.toMutableList()
                },
                onError = {
                    savedBooks = mutableListOf()
                },
            ).addTo(disposables)
    }
    //endregion
}