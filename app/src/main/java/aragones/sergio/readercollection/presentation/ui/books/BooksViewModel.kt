/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.presentation.ui.books

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import aragones.sergio.readercollection.data.remote.ApiManager
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.Constants
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
    private val _books = MutableLiveData<List<Book>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val books: LiveData<List<Book>> = _books
    val readingBooks: LiveData<List<Book>> = _books.map {
        it.filter { book -> book.isReading() }
    }
    val pendingBooks: LiveData<List<Book>> = _books.map {
        it.filter { book -> book.isPending() }.sortedBy { book -> book.priority }
    }
    val readBooks: LiveData<List<Book>> = _books.map {
        it.filter { book -> !book.isReading() && !book.isPending() }
    }
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse?> = _booksError

    val readingBooksVisible: LiveData<Boolean> = readingBooks.map {
        !((it.isEmpty() && query.isNotBlank()) || books.value?.isEmpty() == true)
    }
    val pendingBooksVisible: LiveData<Boolean> = pendingBooks.map {
        it.isNotEmpty()
    }
    val seeMorePendingBooksVisible: LiveData<Boolean> = pendingBooks.map {
        it.size > Constants.BOOKS_TO_SHOW
    }
    val readBooksVisible: LiveData<Boolean> = readBooks.map {
        it.isNotEmpty()
    }
    val seeMoreReadBooksVisible: LiveData<Boolean> = readBooks.map {
        it.size > Constants.BOOKS_TO_SHOW
    }
    val noResultsVisible: LiveData<Boolean> = _books.map {
        it.isEmpty()
    }
    var sortParam = userRepository.sortParam
    var isSortDescending = userRepository.isSortDescending
    var query: String = ""
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
        _booksLoading.value = true
        booksRepository
            .getBooks()
            .subscribeBy(
                onComplete = {
                    originalBooks.value = listOf()
                    _books.value = listOf()
                    _booksLoading.value = false
                },
                onNext = {
                    originalBooks.value = it
                    sortBooks()
                    _booksLoading.value = false
                },
                onError = {
                    _booksLoading.value = false
                    _booksError.value = ApiManager.handleError(it)
                    _booksError.value = null
                },
            ).addTo(disposables)
    }

    fun sort(context: Context, acceptHandler: (() -> Unit)?) {
        super.sort(context, sortParam, isSortDescending) { newSortParam, newIsSortDescending ->

            sortParam = newSortParam
            isSortDescending = newIsSortDescending
            sortBooks()
            acceptHandler?.invoke()
        }
    }

    fun searchBooks(query: String) {
        this.query = query
        sortBooks()
    }

    fun setTutorialAsShown() {
        userRepository.setHasBooksTutorialBeenShown(true)
        tutorialShown = true
    }

    fun setPriorityFor(books: List<Book>) {
        _booksLoading.value = true
        booksRepository.setBooks(books, success = {
            searchBooks(query)
            _booksLoading.value = false
        }, failure = {
            _booksLoading.value = false
            _booksError.value = it
        })
    }
    //endregion

    private fun sortBooks() {
        val filteredBooks = originalBooks.value?.filter { book ->
            (book.title?.contains(query, true) ?: false) ||
                book.authorsToString().contains(query, true)
        } ?: listOf()
        val sortedBooks = when (sortParam) {
            "title" -> filteredBooks.sortedBy { it.title }
            "publishedDate" -> filteredBooks.sortedBy { it.publishedDate }
            "readingDate" -> filteredBooks.sortedBy { it.readingDate }
            "pageCount" -> filteredBooks.sortedBy { it.pageCount }
            "rating" -> filteredBooks.sortedBy { it.rating }
            else -> filteredBooks.sortedBy { it.id }
        }
        _books.value = if (isSortDescending) sortedBooks.reversed() else sortedBooks
    }
}