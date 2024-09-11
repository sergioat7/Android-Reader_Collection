/*
 * Copyright (c) 2024 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/1/2022
 */

package aragones.sergio.readercollection.presentation.ui.booklist

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.data.remote.model.ErrorResponse
import aragones.sergio.readercollection.domain.BooksRepository
import aragones.sergio.readercollection.domain.UserRepository
import aragones.sergio.readercollection.domain.model.Book
import aragones.sergio.readercollection.presentation.ui.base.BaseViewModel
import com.aragones.sergio.util.BookState
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
    private val arePendingBooks: Boolean
        get() = state == BookState.PENDING
    private val _books = MutableLiveData<List<Book>?>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
    private val _infoDialogMessageId = MutableLiveData(-1)
    //endregion

    //region Public properties
    var query: String = state["query"] ?: ""
    val books: LiveData<List<Book>?> = _books
    val booksLoading: LiveData<Boolean> = _booksLoading
    val booksError: LiveData<ErrorResponse> = _booksError
    val infoDialogMessageId: LiveData<Int> = _infoDialogMessageId
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
        booksRepository.getBooks().subscribeBy(
            onComplete = {
                noBooksError()
            },
            onNext = {

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

    fun closeDialogs() {
        _infoDialogMessageId.value = -1
    }

    fun sort(context: Context, acceptHandler: (() -> Unit)?) {
        super.sort(context, sortParam, isSortDescending) { newSortParam, newIsSortDescending ->

            sortParam = newSortParam
            isSortDescending = newIsSortDescending
            fetchBooks()
            acceptHandler?.invoke()
        }
    }

    fun setPriorityFor(books: List<Book>) {

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
    private fun showBooks(books: List<Book>) {

        var filteredBooks = books
            .filter { book ->

                var condition = true
                format?.let {
                    condition = book.format == it
                }
                if (state.isNotEmpty()) {
                    condition = condition && book.state == state
                }
                condition
            }
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

        val sortComparator = compareBy<Book> {
            when (sortParam) {
                "title" -> it.title
                "publishedDate" -> it.publishedDate
                "readingDate" -> it.readingDate
                "pageCount" -> it.pageCount
                "rating" -> it.rating
                else -> it.id
            }
        }
        val comparator = if (arePendingBooks) {
            compareBy<Book> { it.priority }.then(sortComparator)
        } else {
            sortComparator.thenBy { it.priority }
        }
        _books.value = filteredBooks.sortedWith(comparator)
            .let { if (isSortDescending && !arePendingBooks) it.reversed() else it }
    }

    private fun noBooksError() {

        _booksLoading.value = false
        _booksError.value = ErrorResponse("", R.string.error_database)
    }
    //endregion
}