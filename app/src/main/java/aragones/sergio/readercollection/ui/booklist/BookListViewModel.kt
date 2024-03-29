/*
 * Copyright (c) 2022 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.ui.booklist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.ui.base.BaseViewModel
import com.aragones.sergio.data.business.BookResponse
import com.aragones.sergio.data.business.ErrorResponse
import com.aragones.sergio.util.ScrollPosition
import com.aragones.sergio.util.State
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
    private val userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private var state: String = state["state"] ?: ""
    private var sortParam: String? = state["sortParam"]
    private var isSortDescending: Boolean = state["isSortDescending"] ?: false
    private var year: Int = state["year"] ?: -1
    private var month: Int = state["month"] ?: -1
    private var author: String? = state["author"]
    private var format: String? = state["format"]
    private val _books = MutableLiveData<List<BookResponse>>(listOf())
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _scrollPosition = MutableLiveData(ScrollPosition.TOP)
    //endregion

    //region Public properties
    var query: String = state["query"] ?: ""
    val books: LiveData<List<BookResponse>> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val scrollPosition: LiveData<ScrollPosition> = _scrollPosition
    val arePendingBooks: Boolean
        get() = state == State.PENDING
    var tutorialShown = userRepository.hasDragTutorialBeenShown
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
            format,
            state,
            null,
            sortParam
        ).subscribeBy(
            onComplete = {
                noBooksError()
            },
            onSuccess = {

                if (it.isEmpty()) {
                    noBooksError()
                } else {

                    showBooks(it)
                    _booksLoading.value = false
                }
            },
            onError = {
                noBooksError()
            }
        ).addTo(disposables)
    }

    fun setPosition(newPosition: ScrollPosition) {
        _scrollPosition.value = newPosition
    }

    fun sort(context: Context, acceptHandler: (() -> Unit)?) {
        super.sort(context, sortParam, isSortDescending) { newSortParam, newIsSortDescending ->

            sortParam = newSortParam
            isSortDescending = newIsSortDescending
            fetchBooks()
            acceptHandler?.invoke()
        }
    }

    fun setPriorityFor(books: List<BookResponse>) {

        _booksLoading.value = true
        booksRepository.setBooks(books, success = {

            showBooks(books)
            _booksLoading.value = false
        }, failure = {

            _booksLoading.value = false
            _booksError.value = it
        })
    }

    fun setTutorialAsShown() {

        userRepository.setHasDragTutorialBeenShown(true)
        tutorialShown = true
    }
    //endregion

    //region Private methods
    private fun showBooks(books: List<BookResponse>) {

        val sortedBooks = if (isSortDescending) books.reversed() else books
        var filteredBooks = sortedBooks
            .filter { book ->
                (book.title?.contains(query, true) ?: false)
                    || book.authorsToString().contains(query, true)
            }.filter { book ->
                book.authorsToString().contains(author ?: "")
            }
        if (year >= 0) {
            filteredBooks = filteredBooks.filter { book ->
                book.readingDate.getYear() == year
            }
        }
        if (month in 0..11) {
            filteredBooks = filteredBooks.filter { book ->
                book.readingDate.getMonthNumber() == month
            }
        }
        _books.value = filteredBooks.sortedBy { it.priority }
    }

    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
    }
    //endregion
}