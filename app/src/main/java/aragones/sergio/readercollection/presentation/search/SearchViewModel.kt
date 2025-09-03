/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.search

import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.di.IoScheduler
import aragones.sergio.readercollection.domain.di.MainScheduler
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.base.BaseViewModel
import com.aragones.sergio.util.BookState
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.rx3.asFlowable
import kotlinx.coroutines.rx3.rxCompletable
import kotlinx.coroutines.rx3.rxSingle

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    @IoScheduler private val ioScheduler: Scheduler,
    @MainScheduler private val mainScheduler: Scheduler,
) : BaseViewModel() {

    //region Private properties
    private var query = ""
    private var page: Int = 1
    private val books = mutableListOf<Book>()
    private lateinit var savedBooks: MutableList<Book>
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
    init {
        fetchPendingBooks()
    }

    fun onResume() {
        fetchPendingBooks()
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

        _state.value = when (val currentState = _state.value) {
            is SearchUiState.Empty -> SearchUiState.Success(
                isLoading = true,
                query = this.query,
                books = listOf(),
            )
            is SearchUiState.Success -> currentState.copy(isLoading = true)
            is SearchUiState.Error -> currentState.copy(isLoading = true)
        }

        rxSingle {
            booksRepository
                .searchBooks(this@SearchViewModel.query, page, null)
        }.subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
            .subscribeBy(
                onSuccess = { result ->
                    result.fold(
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
                                query = this.query,
                                books = updatedBooks,
                            )
                        },
                        onFailure = {
                            _state.value = SearchUiState.Error(
                                isLoading = false,
                                query = this.query,
                                value = ErrorResponse("", R.string.error_search),
                            )
                        },
                    )
                },
                onError = {
                    _state.value = SearchUiState.Error(
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
        rxCompletable {
            booksRepository
                .createBook(newBook)
        }.subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
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
            .asFlowable()
            .subscribeOn(ioScheduler)
            .observeOn(mainScheduler)
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