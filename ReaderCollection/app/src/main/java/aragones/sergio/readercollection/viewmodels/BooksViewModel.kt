/*
 * Copyright (c) 2020 Sergio Aragonés. All rights reserved.
 * Created by Sergio Aragonés on 18/10/2020
 */

package aragones.sergio.readercollection.viewmodels

import android.content.Context
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import aragones.sergio.readercollection.R
import aragones.sergio.readercollection.base.BaseViewModel
import aragones.sergio.readercollection.extensions.getPickerParams
import aragones.sergio.readercollection.extensions.setup
import aragones.sergio.readercollection.models.responses.BookResponse
import aragones.sergio.readercollection.models.responses.ErrorResponse
import aragones.sergio.readercollection.network.ApiManager
import aragones.sergio.readercollection.repositories.BooksRepository
import aragones.sergio.readercollection.repositories.UserRepository
import aragones.sergio.readercollection.utils.Constants
import aragones.sergio.readercollection.utils.State
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

class BooksViewModel @Inject constructor(
    private val booksRepository: BooksRepository,
    userRepository: UserRepository
) : BaseViewModel() {

    //region Private properties
    private val _originalBooks = MutableLiveData<List<BookResponse>>()
    private val _books = MutableLiveData<List<BookResponse>>()
    private val _booksLoading = MutableLiveData<Boolean>()
    private val _booksError = MutableLiveData<ErrorResponse>()
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
    val booksError: LiveData<ErrorResponse> = _booksError

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
    //endregion

    //region Lifecycle methods
    override fun onDestroy() {
        super.onDestroy()
        booksRepository.onDestroy()
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
                onDestroy()
            }
        ).addTo(disposables)
    }

    fun sort(context: Context) {

        val sortingKeys = context.resources.getStringArray(R.array.sorting_param_keys)
        val sortingValues = context.resources.getStringArray(R.array.sorting_param_values)

        val dialogView = LinearLayout(context)
        dialogView.orientation = LinearLayout.HORIZONTAL

        val sortKeysPicker = NumberPicker(context)
        sortKeysPicker.setup(sortingValues)
        sortParam?.let {
            sortKeysPicker.value = sortingKeys.indexOf(it)
        }

        val sortOrdersPicker = NumberPicker(context)
        sortOrdersPicker.setup(context.resources.getStringArray(R.array.sorting_order_values))
        sortOrdersPicker.value = if (isSortDescending) 1 else 0

        val params = LinearLayout.LayoutParams(50, 50)
        params.gravity = Gravity.CENTER

        dialogView.layoutParams = params
        dialogView.addView(sortKeysPicker, getPickerParams())
        dialogView.addView(sortOrdersPicker, getPickerParams())

        MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.order_by))
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton(context.resources.getString(R.string.accept)) { dialog, _ ->

                val sort = sortingKeys[sortKeysPicker.value]
                sortParam = sort.ifBlank { null }
                isSortDescending = sortOrdersPicker.value == 1
                fetchBooks()
                dialog.dismiss()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun searchBooks(query: String) {

        this.query = query
        _books.value = _originalBooks.value?.filter { book ->
            book.title?.contains(query, true) ?: false
        } ?: listOf()
    }
    //endregion
}