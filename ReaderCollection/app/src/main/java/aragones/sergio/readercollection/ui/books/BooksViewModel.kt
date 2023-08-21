/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.ui.books

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import aragones.sergio.readercollection.data.source.BooksRepository
import aragones.sergio.readercollection.data.source.UserRepository
import aragones.sergio.readercollection.models.BookResponse
import aragones.sergio.readercollection.models.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.ui.base.BaseViewModel
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _originalBooks = MutableLiveData<List<BookResponse>>()
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse?>()
    //endregion

    //region Public properties
    val books: LiveData<List<BookResponse>> = _books
    val readingBooks: LiveData<List<BookResponse>> = _books.map {
        it.filter { book -> book.state == State.READING }
    }
    val pendingBooks: LiveData<List<BookResponse>> = _books.map {
        it.filter { book -> book.state == State.PENDING }
    }
    val readBooks: LiveData<List<BookResponse>> = _books.map {
        it.filter { book -> book.state != State.READING && book.state != State.PENDING }
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
        booksRepository.getBooksDatabaseObserver(
            null,
            null,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {

                _originalBooks.value = listOf()
                _books.value = listOf()
                _booksLoading.value = false
            },
            onSuccess = {

                _originalBooks.value = if (isSortDescending) it.reversed() else it
                searchBooks(query)
                _booksLoading.value = false
            },
            onError = {

                _booksLoading.value = false
                _booksError.value = ApiManager.handleError(it)
                _booksError.value = null
            }
        ).addTo(disposables)
    }

    fun sort(context: Context, acceptHandler: (() -> Unit)?) {
        super.sort(context, sortParam, isSortDescending) { newSortParam, newIsSortDescending ->

            sortParam = newSortParam
            isSortDescending = newIsSortDescending
            fetchBooks()
            acceptHandler?.invoke()
        }
    }

    fun searchBooks(query: String) {

        this.query = query
        _books.value = _originalBooks.value?.filter { book ->
            book.title?.contains(query, true) ?: false
        } ?: listOf()
    }

    fun setTutorialAsShown() {
        userRepository.setHasBooksTutorialBeenShown(true)
        tutorialShown = true
    }
    //endregion
}